package tachiyomi.ui.catalogbrowse

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.processors.BehaviorProcessor
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.core.stdlib.replaceFirst
import tachiyomi.data.catalog.prefs.CatalogPreferences
import tachiyomi.domain.manga.interactor.ListMangaPageFromCatalogSource
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.interactor.SearchMangaPageFromCatalogSource
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangasPage
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.ui.base.BasePresenter
import tachiyomi.ui.catalogbrowse.Action.DisplayModeUpdated
import tachiyomi.ui.catalogbrowse.Action.ErrorDelivered
import tachiyomi.ui.catalogbrowse.Action.LoadMore
import tachiyomi.ui.catalogbrowse.Action.Loading
import tachiyomi.ui.catalogbrowse.Action.LoadingError
import tachiyomi.ui.catalogbrowse.Action.MangaInitialized
import tachiyomi.ui.catalogbrowse.Action.PageReceived
import tachiyomi.ui.catalogbrowse.Action.QueryModeUpdated
import tachiyomi.ui.catalogbrowse.Action.SwapDisplayMode
import timber.log.Timber
import javax.inject.Inject

/**
 * Presenter of the catalog that interacts with the backend through use cases and also handles
 * intentions from the [CatalogBrowseController]. Any kind of update produces a [Change] which is
 * then reduced to a new [CatalogBrowseViewState]. Updates from the controller usually emit a new
 * [Action], that may also generate one or more [Change] and a new view state.
 *
 * [params] contains the required parameters of this presenter.
 * [sourceManager] contains the manager used to retrieve catalogs.
 * [listMangaPageFromCatalogSource] is an use case that retrieves a list of manga from a catalog
 * given a listing.
 * [searchMangaPageFromCatalogSource] is an use case that retrieves a list of manga from a catalog
 * given a list of filters.
 * [mangaInitializer] is an use case that initializes the given manga if needed.
 * [catalogPreferences] contains preferences used by the catalog.
 */
class CatalogBrowsePresenter @Inject constructor(
  private val params: CatalogBrowseParams,
  private val sourceManager: SourceManager, // TODO new use case to retrieve a catalogue?
  private val listMangaPageFromCatalogSource: ListMangaPageFromCatalogSource,
  private val searchMangaPageFromCatalogSource: SearchMangaPageFromCatalogSource,
  private val mangaInitializer: MangaInitializer,
  private val catalogPreferences: CatalogPreferences,
  private val schedulers: RxSchedulers
) : BasePresenter() {

  /**
   * Behavior subject containing the last emitted view state.
   */
  val stateRelay = BehaviorProcessor.create<CatalogBrowseViewState>()

  /**
   * Subject which allows emitting actions and subscribing to a specific one while supporting
   * backpressure.
   */
  private val actions = PublishRelay.create<Action>()

  /**
   * Last used listing preference.
   */
  private val lastListingPreference = catalogPreferences.lastListingUsed(params.sourceId)

  /**
   * Grid mode preference.
   */
  private val gridPreference = catalogPreferences.gridMode()

  init {
    // Build the initial view state.
    val initialViewState = getInitialViewState()

    actions
      .observeOn(schedulers.io)
      .doOnNext { Timber.d("Input action $it") }
      .reduxStore(
        initialState = initialViewState,
        sideEffects = listOf(
          ::setListingSideEffect,
          ::setFiltersSideEffect,
          ::setDisplayModeSideEffect,
          ::loadNextSideEffect
        ),
        reducer = ::reducer
      )
      .distinctUntilChanged()
      .doOnNext { Timber.d("RxStore state: $it") }
      .observeOn(schedulers.main)
      .subscribe(stateRelay::onNext)
      .addTo(disposables)
  }

  private fun getInitialViewState(): CatalogBrowseViewState {
    // Find the requested source or early return an initial state with a not found error.
    val source = sourceManager.get(params.sourceId) as? CatalogSource
      ?: return CatalogBrowseViewState(error = Exception("Source not found"))

    // Get the listings of the source and the initial listing and query mode to set.
    val listings = source.getListings()
    val initialListing = listings.getOrNull(lastListingPreference.get()) ?: listings.firstOrNull()
    val initialQueryMode = QueryMode.List(initialListing)

    return CatalogBrowseViewState(
      source = source,
      queryMode = initialQueryMode,
      listings = listings,
      filters = getWrappedFilters(source),
      isGridMode = gridPreference.get()
    )
  }

  private fun setListingSideEffect(
    actions: Observable<Action>,
    state: StateAccessor<CatalogBrowseViewState>
  ): Observable<Action> {
    return actions.ofType(Action.SetSearchMode.Listing::class.java)
      .flatMap { action ->
        val currentState = state()
        val type = currentState.listings.getOrNull(action.index)
        if (type == null) {
          // Do nothing if the index is out of bounds.
          Observable.empty()
        } else {
          // Save this listing as the last used.
          lastListingPreference.set(action.index)

          // Emit an update to listing mode.
          val queryMode = QueryMode.List(type)
          Observable.just(Action.QueryModeUpdated(queryMode))
        }
      }
  }

  private fun setFiltersSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<CatalogBrowseViewState>
  ): Observable<Action> {
    return actions.ofType(Action.SetSearchMode.Filters::class.java)
      .flatMap { action ->
        // Get the filters to apply, update their inner value and ignore the ones with the default
        // value.
        val filters = action.filters
          .asSequence()
          .onEach { it.updateInnerValue() }
          .map { it.filter }
          .filter { !it.isDefaultValue() }
          .toList()

        if (filters.isEmpty()) {
          // Do nothing if there are no filters to apply.
          Observable.empty()
        } else {
          // Emit an update to search/filter mode.
          val queryMode = QueryMode.Filter(filters)
          Observable.just(Action.QueryModeUpdated(queryMode))
        }
      }
  }

  private fun loadNextSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<CatalogBrowseViewState>
  ): Observable<Action> {
    return actions.filter { it is Action.LoadMore || it is Action.QueryModeUpdated }
      .startWith(Action.LoadMore) // Always load the initial page
      .filter {
        val state = stateFn()
        state.source != null && state.queryMode != null &&
          !state.isLoading && state.hasMorePages
      }
      .switchMap {
        val state = stateFn()
        val source = state.source!!
        val queryMode = state.queryMode!!
        val nextPage = state.currentPage + 1

        val mangasPageSingle = when (queryMode) {
          is QueryMode.Filter ->
            searchMangaPageFromCatalogSource.interact(source, queryMode.filters, nextPage)
          is QueryMode.List ->
            listMangaPageFromCatalogSource.interact(source, queryMode.listing, nextPage)
        }

        mangasPageSingle
          .doOnSubscribe { Timber.w("Requesting page $nextPage") }
          .subscribeOn(schedulers.io)
          .toObservable()
          .flatMap { mangasPage ->
            val pageReceived = Observable.just(Action.PageReceived(mangasPage))

            val mangaInitializer = Observable.fromIterable(mangasPage.mangas)
              .subscribeOn(schedulers.io)
              .concatMapMaybe { mangaInitializer.interact(source, it).onErrorComplete() }
              .map { Action.MangaInitialized(it) }

            Observable.merge(pageReceived, mangaInitializer)
          }
          .startWith(Action.Loading(true, state.currentPage))
          .onErrorReturn(Action::LoadingError)
      }

  }

  /**
   * Returns the changes of display mode updates.
   */
  private fun setDisplayModeSideEffect(
    actions: Observable<Action>,
    state: StateAccessor<CatalogBrowseViewState>
  ): Observable<Action> {
    return actions.ofType(Action.SwapDisplayMode::class.java)
      .flatMap {
        val newValue = !state().isGridMode
        gridPreference.set(newValue)
        Observable.just(Action.DisplayModeUpdated(newValue))
      }
  }

  /**
   * Returns the filters of the given [source] wrapped in a container that allows to keep the values
   * of the filters in sync with the UI without changing their internal value.
   */
  private fun getWrappedFilters(source: CatalogSource): List<FilterWrapper<*>> {
    val filters = mutableListOf<FilterWrapper<*>>()
    source.getFilters().forEach { filter ->
      filters.add(FilterWrapper.from(filter))
      if (filter is Filter.Group) {
        val childFilters = filter.filters.map { FilterWrapper.from(it) }
        filters.addAll(childFilters)
      }
    }
    return filters
  }

  /**
   * Emits an action to request a display mode change.
   */
  fun swapDisplayMode() {
    actions.accept(Action.SwapDisplayMode)
  }

  /**
   * Emits an action to request the next page of the catalog.
   */
  fun loadMore() {
    actions.accept(Action.LoadMore)
  }

  /**
   * Emits an action to query the catalog with the listing at the given [index].
   */
  fun setListing(index: Int) {
    actions.accept(Action.SetSearchMode.Listing(index))
  }

  /**
   * Emits an action to query the catalog with the given [filters].
   */
  fun setFilters(filters: List<FilterWrapper<*>>) {
    actions.accept(Action.SetSearchMode.Filters(filters))
  }

}

/**
 * List of actions that can be used to request and mutate the view state.
 *
 * [SwapDisplayMode] is used to change the layout manager to a grid or a list.
 * [LoadMore] is used to request the next page of the catalog's current query.
 * [SetListing] is used to set a new query on the catalog with the given listing.
 * [SetFilters] is used to set a new query on the catalog with the given filters.
 * [ErrorDelivered] is used to notify the presenter that the UI has received the error.
 *
 * [QueryModeUpdated] sets a new query mode. It's the result of applying a [SetListing] or
 *   [SetFilters] action.
 * [PageReceived] adds a page received from the catalog to the current list.
 * [DisplayModeUpdated] sets the new display mode. It's the result of applying a [SwapDisplayMode]
 *   action.
 * [MangaInitialized] replaces the initialized manga with the non-initialized on the current list.
 * [Loading] sets the loading state, and also sets an empty list of manga if it's the first page.
 * [LoadingError] sets the error that can occur when the requested page fails to load.
 */
private sealed class Action {

  object SwapDisplayMode : Action()
  object LoadMore : Action()
  object ErrorDelivered : Action()
  sealed class SetSearchMode : Action() {
    data class Listing(val index: Int) : SetSearchMode()
    data class Filters(val filters: List<FilterWrapper<*>>) : SetSearchMode()
  }

  data class QueryModeUpdated(val mode: QueryMode) : Action()
  data class PageReceived(val page: MangasPage) : Action()
  data class DisplayModeUpdated(val isGridMode: Boolean) : Action()
  data class MangaInitialized(val manga: Manga) : Action()
  data class Loading(val isLoading: Boolean, val page: Int) : Action()
  data class LoadingError(val error: Throwable?) : Action()

}

/**
 * Function that reduces an [action] into a new [CatalogBrowseViewState] given the current [state].
 * The resulting view state is later emitted through a [BehaviorProcessor] from which the UI
 * receives the updates.
 */
private fun reducer(state: CatalogBrowseViewState, action: Action): CatalogBrowseViewState {
  return when (action) {
    is Action.QueryModeUpdated -> state.copy(
      queryMode = action.mode,
      mangas = emptyList(),
      currentPage = 0,
      hasMorePages = true,
      isLoading = false
    )
    is Action.PageReceived -> state.copy(
      mangas = state.mangas + action.page.mangas,
      isLoading = false,
      currentPage = action.page.number,
      hasMorePages = action.page.hasNextPage
    )
    is Action.DisplayModeUpdated -> state.copy(isGridMode = action.isGridMode)
    is Action.MangaInitialized -> state.copy(mangas = state.mangas
      .replaceFirst({ it.id == action.manga.id }, action.manga)
    )
    is Action.Loading -> state.copy(
      isLoading = action.isLoading
    )
    is Action.LoadingError -> state.copy(error = action.error, isLoading = false)
    is Action.ErrorDelivered -> state.copy(error = null)
    else -> state
  }
}
