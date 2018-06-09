package tachiyomi.ui.catalogbrowse

import io.reactivex.Flowable
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
import tachiyomi.source.model.SearchQuery
import tachiyomi.source.model.Sorting
import tachiyomi.ui.base.BasePresenter
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

  private var currentState = CatalogBrowseViewState()

  private val actions = ActionsPublisher<Action>()

  init {
    bindChanges()
      .scan(currentState, ::reduce)
      .logOnNext()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext { currentState = it }
      .subscribe(stateRelay::onNext)
      .addTo(disposables)
  }

  private fun bindChanges(): Flowable<Change> {
    val source = sourceManager.get(sourceId!!) as? CatalogSource
      ?: return Flowable.just(Change.SourceNotFound)

    val sortings = source.getSortings()

    val gridPreference = catalogPreferences.gridMode()
    val lastSortingUsedPreference = catalogPreferences.lastSortingUsed(sourceId)

    currentState = CatalogBrowseViewState(
      source = source,
      sortings = sortings,
      activeSorting = sortings.getOrNull(lastSortingUsedPreference.get()),
      isGridMode = gridPreference.get()
    )

    val mangaInitializerSubject = PublishProcessor.create<List<Manga>>().toSerialized()

    val queryChange = bindQueryChange()
    val sortingChange = bindSortingChange(sortings, lastSortingUsedPreference)
      .startWith(Change.SortingUpdate(currentState.activeSorting))
      .switchMap {
        Flowable.merge(
          Flowable.just(it),
          bindPageLoader(source, it.sorting, mangaInitializerSubject)
        )
      }
    val mangaInitialized = bindMangaInitialized(source, mangaInitializerSubject)
    val displayMode = bindDisplayMode(gridPreference)
    val errorDelivered = bindErrorDelivered()

    val changes = listOf(queryChange, sortingChange, mangaInitialized, displayMode,
      errorDelivered)

    return Flowable.merge(changes)
  }

  private fun bindPageLoader(
    source: CatalogSource,
    sorting: Sorting?,
    mangaInitializerSubject: FlowableProcessor<List<Manga>>
  ): Flowable<Change> {
    return actions.ofType(Action.PerformSearch::class)
      // Always perform initial search
      .startWith(Action.PerformSearch("", emptyList()))
      .distinctUntilChanged()
      .switchMap { action ->
        val currentPage = AtomicInteger(1)
        val hasNextPage = AtomicBoolean(true)

        val query = SearchQuery(sorting, action.query, action.filters)

        actions.ofType(Action.LoadMore::class)
          .startWith(Action.LoadMore) // Always load the initial page
          .map { currentPage.get() }
          .concatMap f@{ requestedPage ->
            val page = currentPage.get()
            if (!hasNextPage.get() || requestedPage < page) return@f Flowable.empty<Change>()

            getMangaPageFromCatalogSource.interact(source, query, page)
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
    return actions.ofType(Action.Query::class)
      .flatMap { action ->
        Flowable.just(Change.QueryUpdate(action.query))
          .doOnNext { performSearch(it.query) }
      }
      .map { Change.QueryUpdate(it.query) }
  }

  private fun bindSortingChange(
    types: List<Sorting>,
    lastSortingUsedPreference: Preference<Int>
  ): Flowable<Change.SortingUpdate> {
    return actions.ofType(Action.Sorting::class)
      .flatMap { action ->
        val type = types.getOrNull(action.index)
        if (type == null) {
          Flowable.empty()
        } else {
          lastSortingUsedPreference.set(action.index)
          Flowable.just(Change.SortingUpdate(type))
        }
      }
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
    return actions.ofType(Action.SwapDisplayMode::class)
      .map {
        val newValue = !gridPreference.get()
        gridPreference.set(newValue)
        newValue
      }
      .map(Change::DisplayModeUpdate)
  }

  private fun bindErrorDelivered(): Flowable<Change> {
    return actions.ofType(Action.ErrorDelivered::class)
      .map { Change.LoadingError(null) }
  }

  fun performSearch(query: String? = null, filters: FilterList? = null) {
    val actionQuery = query ?: currentState.query
    val actionFilters = filters ?: currentState.filters
    actions.emit(Action.PerformSearch(actionQuery, actionFilters))
  }

  fun swapDisplayMode() {
    actions.emit(Action.SwapDisplayMode)
  }

  fun setQuery(query: String) {
    actions.emit(Action.Query(query))
  }

  fun loadMore() {
    actions.emit(Action.LoadMore)
  }

  fun setSorting(index: Int) {
    actions.emit(Action.Sorting(index))
  }

}

private sealed class Action {
  object SwapDisplayMode : Action()
  object LoadMore : Action()
  data class Sorting(val index: Int) : Action()
  data class Query(val query: String) : Action()
  data class PerformSearch(val query: String, val filters: FilterList) : Action()
  object ErrorDelivered : Action()
}

private sealed class Change {
  data class SortingUpdate(val sorting: Sorting?) : Change()
  data class PageReceived(val page: MangasPage) : Change()
  data class DisplayModeUpdate(val isGridMode: Boolean) : Change()
  data class MangaInitialized(val manga: Manga) : Change()
  data class QueryUpdate(val query: String) : Change()
  data class Loading(val isLoading: Boolean, val page: Int) : Change()
  data class LoadingError(val error: Throwable?) : Change()
  object SourceNotFound : Change()
}

private fun reduce(state: CatalogBrowseViewState, change: Change): CatalogBrowseViewState {
  return when (change) {
    is Change.SortingUpdate -> state.copy(
      activeSorting = change.sorting,
      query = "",
      filters = emptyList()
    )
    is Change.PageReceived -> state.copy(
      mangas = state.mangas + change.page.mangas,
      isLoading = false,
      hasMorePages = change.page.hasNextPage
    )
    is Change.DisplayModeUpdate -> state.copy(isGridMode = change.isGridMode)
    is Change.MangaInitialized -> state.copy(mangas = state.mangas
      .replaceFirst({ it.id == change.manga.id }, change.manga)
    )
    is Change.QueryUpdate -> state.copy(query = state.query)
    is Change.Loading -> state.copy(
      isLoading = change.isLoading,
      hasMorePages = true,
      mangas = if (change.isLoading && change.page == 1) emptyList() else state.mangas
    )
    is Change.LoadingError -> state.copy(error = change.error, isLoading = false)
    is Change.SourceNotFound -> state.copy(error = Exception("Source not found"))
  }
}
