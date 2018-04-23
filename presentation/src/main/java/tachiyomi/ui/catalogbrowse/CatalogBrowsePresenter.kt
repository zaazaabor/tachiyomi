package tachiyomi.ui.catalogbrowse

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import tachiyomi.core.rx.addTo
import tachiyomi.core.stdlib.replaceFirst
import tachiyomi.data.catalog.prefs.CatalogPreferences
import tachiyomi.domain.manga.interactor.GetMangaPageFromCatalogueSource
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.CatalogueSource
import tachiyomi.domain.source.SourceManager
import tachiyomi.ui.base.BasePresenter
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class CatalogBrowsePresenter @Inject constructor(
  private val sourceId: Long?,
  private val sourceManager: SourceManager, // TODO new use case to retrieve a catalogue?
  private val getMangaPageFromCatalogueSource: GetMangaPageFromCatalogueSource,
  private val mangaInitializer: MangaInitializer,
  private val catalogPreferences: CatalogPreferences
) : BasePresenter() {

  val stateRelay = BehaviorProcessor.create<CatalogBrowseViewState>()

  private val actionsRelay = PublishProcessor.create<Action>()

  private val actionsObserver = actionsRelay.onBackpressureBuffer().share()

  init {
    val gridPreference = catalogPreferences.gridMode()

    val initialState = CatalogBrowseViewState(isGridMode = gridPreference.get())

    val mangaInitializerSubject = PublishProcessor.create<List<Manga>>().toSerialized()

    val sourceRelay = Flowable.fromCallable { sourceManager.get(sourceId!!) as? CatalogueSource }
      .share()

    val sourceChange = sourceRelay.map(Change::SourceUpdate)

    val pageLoader = sourceRelay
      .switchMap { source ->
        val currentPage = AtomicInteger(1)
        val hasNextPage = AtomicBoolean(true)

        actionsObserver.ofType(Action.LoadMore::class.java)
          .startWith(Action.LoadMore) // Always load the initial page
          .map { currentPage.get() }
          .concatMap f@{ requestedPage ->
            val page = currentPage.get()
            if (!hasNextPage.get()) return@f Flowable.just(Change.EndReached)
            if (requestedPage < page) return@f Flowable.empty<Change>()


            getMangaPageFromCatalogueSource.interact(source, page)
              .subscribeOn(Schedulers.io())
              .doOnSuccess { mangasPage ->
                mangaInitializerSubject.onNext(mangasPage.mangas)
                hasNextPage.set(mangasPage.hasNextPage)
                currentPage.incrementAndGet()
              }
              .map<Change> { Change.PageReceived(it.mangas) }
              .toFlowable()
              .onErrorReturn(Change::LoadingError)
              .startWith(Change.Loading(true))
          }
      }

    val mangaInitialized = sourceRelay
      .switchMap { source ->
        mangaInitializerSubject
          .observeOn(Schedulers.io())
          .flatMapIterable { it }
          .concatMapMaybe { mangaInitializer.interact(source, it) }
          .map(Change::MangaInitialized)
      }

    val queryChange = actionsObserver.ofType(Action.Query::class.java)
      .debounce { if (it.submit) Flowable.empty() else Flowable.timer(1250, MILLISECONDS) }
      .map { Change.QueryUpdate(it.query) }
      .distinctUntilChanged()

    val displayMode = actionsObserver.ofType(Action.SwapDisplayMode::class.java)
      .map {
        val newValue = !gridPreference.get()
        gridPreference.set(newValue)
        newValue
      }
      .map(Change::DisplayModeUpdate)

    val errorDelivered = actionsObserver.ofType(Action.ErrorDelivered::class.java)
      .map { Change.LoadingError(null) }

    val intents = listOf(sourceChange, pageLoader, mangaInitialized, queryChange, displayMode,
      errorDelivered)

    Flowable.merge(intents)
      .scan(initialState, ::reduce)
      .logOnNext()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(stateRelay::onNext)
      .addTo(disposables)
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
  object ErrorDelivered : Action()
}

private sealed class Change {
  data class SourceUpdate(val source: CatalogueSource) : Change()
  data class PageReceived(val mangas: List<Manga>) : Change()
  data class DisplayModeUpdate(val isGridMode: Boolean) : Change()
  data class MangaInitialized(val manga: Manga) : Change()
  data class QueryUpdate(val query: String) : Change()
  data class Loading(val isLoading: Boolean) : Change()
  data class LoadingError(val error: Throwable?) : Change()
  object EndReached : Change()
}

private fun reduce(state: CatalogBrowseViewState, change: Change): CatalogBrowseViewState {
  return when (change) {
    is Change.SourceUpdate -> state.copy(source = change.source, mangas = emptyList(),
      hasMorePages = true)
    is Change.PageReceived -> state.copy(mangas = state.mangas + change.mangas,
      isLoading = false)
    is Change.DisplayModeUpdate -> state.copy(isGridMode = change.isGridMode)
    is Change.MangaInitialized -> state.copy(mangas = state.mangas
      .replaceFirst({ it.id == change.manga.id }, change.manga)
    )
    is Change.QueryUpdate -> state.copy(query = change.query)
    is Change.Loading -> state.copy(isLoading = change.isLoading)
    is Change.LoadingError -> state.copy(error = change.error, isLoading = false)
    is Change.EndReached -> state.copy(hasMorePages = false)
  }
}
