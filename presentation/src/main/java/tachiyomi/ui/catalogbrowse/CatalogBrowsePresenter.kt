package tachiyomi.ui.catalogbrowse

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import tachiyomi.core.prefs.Preference
import tachiyomi.core.rx.addTo
import tachiyomi.core.stdlib.replaceFirst
import tachiyomi.data.catalog.prefs.CatalogPreferences
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.interactor.SearchMangaPageFromCatalogSource
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangasPage
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.FilterList
import tachiyomi.ui.base.BasePresenter
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class CatalogBrowsePresenter @Inject constructor(
  private val sourceId: Long?,
  private val sourceManager: SourceManager, // TODO new use case to retrieve a catalogue?
  private val getMangaPageFromCatalogSource: SearchMangaPageFromCatalogSource,
  private val mangaInitializer: MangaInitializer,
  private val catalogPreferences: CatalogPreferences
) : BasePresenter() {

  val stateRelay = BehaviorProcessor.create<CatalogBrowseViewState>()

  private var currentState: CatalogBrowseViewState

  private val actionsRelay = PublishProcessor.create<Action>()

  private val actionsObserver = actionsRelay.onBackpressureBuffer().share()

  init {
    val gridPreference = catalogPreferences.gridMode()

    currentState = CatalogBrowseViewState(isGridMode = gridPreference.get())

    val sourceIntents = Maybe.fromCallable { sourceManager.get(sourceId!!) as? CatalogSource }
      .flatMapPublisher(::bindIntentsToSource)

    val displayMode = bindDisplayMode(gridPreference)
    val errorDelivered = bindErrorDelivered()

    val intents = listOf(sourceIntents, displayMode, errorDelivered)

    Flowable.merge(intents)
      .scan(currentState, ::reduce)
      .logOnNext()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext { currentState = it }
      .subscribe(stateRelay::onNext)
      .addTo(disposables)
  }

  private fun bindIntentsToSource(source: CatalogSource): Flowable<Change> {
    val mangaInitializerSubject = PublishProcessor.create<List<Manga>>().toSerialized()

    val activeSource = Flowable.just(Change.SourceUpdate(source))
    val queryChange = bindQueryChange()
    val pageLoader = bindPageLoader(source, mangaInitializerSubject)
    val mangaInitialized = bindMangaInitialized(source, mangaInitializerSubject)

    val intents = listOf(activeSource, queryChange, pageLoader, mangaInitialized)

    return Flowable.merge(intents)
  }

  private fun bindPageLoader(
    source: CatalogSource,
    mangaInitializerSubject: FlowableProcessor<List<Manga>>
  ): Flowable<Change> {
    return actionsObserver.ofType(Action.PerformSearch::class.java)
      // Always perform initial search
      .startWith(Action.PerformSearch(currentState.query, currentState.activeFilters))
      .distinctUntilChanged()
      .logOnNext()
      .switchMap { (query, activeFilters) ->
        val currentPage = AtomicInteger(1)
        val hasNextPage = AtomicBoolean(true)

        actionsObserver.ofType(Action.LoadMore::class.java)
          .startWith(Action.LoadMore) // Always load the initial page
          .map { currentPage.get() }
          .concatMap f@{ requestedPage ->
            val page = currentPage.get()
            if (!hasNextPage.get() || requestedPage < page) return@f Flowable.empty<Change>()

            getMangaPageFromCatalogSource.interact(source, page, query, activeFilters)
              .subscribeOn(Schedulers.io())
              .doOnSuccess { mangasPage ->
                mangaInitializerSubject.onNext(mangasPage.mangas)
                hasNextPage.set(mangasPage.hasNextPage)
                currentPage.incrementAndGet()
              }
              .map<Change>(Change::PageReceived)
              .toFlowable()
              .onErrorReturn(Change::LoadingError)
              .startWith(Change.Loading(true, page))
          }
      }
  }

  private fun bindQueryChange(): Flowable<Change.QueryUpdate> {
    return actionsObserver.ofType(Action.Query::class.java)
      .debounce { if (it.submit) Flowable.empty() else Flowable.timer(1250, MILLISECONDS) }
      .flatMap { action ->
        Flowable.just(Change.QueryUpdate(action.query))
          .doOnNext { performSearch(it.query) }
      }
      .map { Change.QueryUpdate(it.query) }
  }

  private fun bindMangaInitialized(
    source: CatalogSource,
    mangaInitializerSubject: FlowableProcessor<List<Manga>>
  ): Flowable<Change> {
    return mangaInitializerSubject
      .observeOn(Schedulers.io())
      .flatMapIterable { it }
      .concatMapMaybe { mangaInitializer.interact(source, it).onErrorComplete() }
      .map(Change::MangaInitialized)
  }

  private fun bindDisplayMode(gridPreference: Preference<Boolean>): Flowable<Change> {
    return actionsObserver.ofType(Action.SwapDisplayMode::class.java)
      .map {
        val newValue = !gridPreference.get()
        gridPreference.set(newValue)
        newValue
      }
      .map(Change::DisplayModeUpdate)
  }

  private fun bindErrorDelivered(): Flowable<Change> {
    return actionsObserver.ofType(Action.ErrorDelivered::class.java)
      .map { Change.LoadingError(null) }
  }

  fun performSearch(query: String? = null, filters: FilterList? = null) {
    val actionQuery = query ?: currentState.query
    val actionFilters = filters ?: currentState.activeFilters
    actionsRelay.onNext(Action.PerformSearch(actionQuery, actionFilters))
  }

  fun swapDisplayMode() {
    actionsRelay.onNext(Action.SwapDisplayMode)
  }

  fun setQuery(query: String, submit: Boolean) {
    actionsRelay.onNext(Action.Query(query, submit))
  }

  fun loadMore() {
    actionsRelay.onNext(Action.LoadMore)
  }

}

private sealed class Action {
  object SwapDisplayMode : Action()
  object LoadMore : Action()
  data class Query(val query: String, val submit: Boolean) : Action()
  data class PerformSearch(val query: String, val filters: FilterList) : Action()
  object ErrorDelivered : Action()
}

private sealed class Change {
  data class SourceUpdate(val source: CatalogSource) : Change()
  data class PageReceived(val page: MangasPage) : Change()
  data class DisplayModeUpdate(val isGridMode: Boolean) : Change()
  data class MangaInitialized(val manga: Manga) : Change()
  data class QueryUpdate(val query: String) : Change()
  data class Loading(val isLoading: Boolean, val page: Int) : Change()
  data class LoadingError(val error: Throwable?) : Change()
}

private fun reduce(state: CatalogBrowseViewState, change: Change): CatalogBrowseViewState {
  return when (change) {
    is Change.SourceUpdate -> state.copy(source = change.source, mangas = emptyList(),
      sourceFilters = change.source.getFilterList(), activeFilters = emptyList(),
      hasMorePages = true)
    is Change.PageReceived -> state.copy(mangas = state.mangas + change.page.mangas,
      isLoading = false, hasMorePages = change.page.hasNextPage)
    is Change.DisplayModeUpdate -> state.copy(isGridMode = change.isGridMode)
    is Change.MangaInitialized -> state.copy(mangas = state.mangas
      .replaceFirst({ it.id == change.manga.id }, change.manga)
    )
    is Change.QueryUpdate -> state.copy(query = change.query)
    is Change.Loading -> state.copy(isLoading = change.isLoading, hasMorePages = true,
      mangas = if (change.isLoading && change.page == 1) emptyList() else state.mangas)
    is Change.LoadingError -> state.copy(error = change.error, isLoading = false)
  }
}
