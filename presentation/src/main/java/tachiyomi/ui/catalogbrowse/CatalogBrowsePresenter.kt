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
import tachiyomi.domain.manga.interactor.ListMangaPageFromCatalogSource
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.interactor.SearchMangaPageFromCatalogSource
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangasPage
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing
import tachiyomi.ui.base.BasePresenter
import tachiyomi.ui.catalogbrowse.Action.ErrorDelivered
import tachiyomi.ui.catalogbrowse.Action.LoadMore
import tachiyomi.ui.catalogbrowse.Action.SetFilters
import tachiyomi.ui.catalogbrowse.Action.SetListing
import tachiyomi.ui.catalogbrowse.Action.SwapDisplayMode
import tachiyomi.ui.catalogbrowse.Change.DisplayModeUpdate
import tachiyomi.ui.catalogbrowse.Change.Loading
import tachiyomi.ui.catalogbrowse.Change.LoadingError
import tachiyomi.ui.catalogbrowse.Change.MangaInitialized
import tachiyomi.ui.catalogbrowse.Change.PageReceived
import tachiyomi.ui.catalogbrowse.Change.QueryModeUpdate
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
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
  private val catalogPreferences: CatalogPreferences
) : BasePresenter() {

  /**
   * Behavior subject containing the last emitted view state.
   */
  val stateRelay = BehaviorProcessor.create<CatalogBrowseViewState>()

  /**
   * Subject which allows emitting actions and subscribing to a specific one while supporting
   * backpressure.
   */
  private val actions = ActionsPublisher<Action>()

  init {
    bindViewState()
      .logOnNext()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(stateRelay::onNext)
      .addTo(disposables)
  }

  /**
   * Returns a [Flowable] containing the incremental updates of a [CatalogBrowseViewState].
   */
  private fun bindViewState(): Flowable<CatalogBrowseViewState> {
    // Find the requested source or early return an initial state with a not found error.
    val source = sourceManager.get(params.sourceId) as? CatalogSource
      ?: return Flowable.just(CatalogBrowseViewState(error = Exception("Source not found")))

    // Retrieve preferences used by this presenter.
    val gridPreference = catalogPreferences.gridMode()
    val lastListingPreference = catalogPreferences.lastListingUsed(params.sourceId)

    // Get the listings of the source and the initial listing and query mode to set.
    val listings = source.getListings()
    val initialListing = listings.getOrNull(lastListingPreference.get()) ?: listings.firstOrNull()
    val initialQueryMode = QueryMode.List(initialListing)

    // Build the initial view state.
    val initialViewState = CatalogBrowseViewState(
      source = source,
      queryMode = initialQueryMode,
      listings = listings,
      filters = getWrappedFilters(source),
      isGridMode = gridPreference.get()
    )

    // Setup all the partial updates (changes).
    val mangaInitializerSubject = PublishProcessor.create<List<Manga>>().toSerialized()

    val queryModeChange = bindQueryModeChange(listings, lastListingPreference)
      .startWith(Change.QueryModeUpdate(initialQueryMode))
      .switchMap {
        Flowable.merge(
          Flowable.just(it),
          bindPageLoader(source, it.mode, mangaInitializerSubject)
        )
      }
    val mangaInitialized = bindMangaInitialized(source, mangaInitializerSubject)
    val displayMode = bindDisplayMode(gridPreference)
    val errorDelivered = bindErrorDelivered()

    val changes = listOf(queryModeChange, mangaInitialized, displayMode,
      errorDelivered)

    // Finally collect the changes and reduce them into view states.
    return Flowable.merge(changes)
      .scan(initialViewState, ::reduce)
  }

  /**
   * Returns the changes of query mode updates, either when the user requests it or by the initial
   * query.
   */
  private fun bindQueryModeChange(
    types: List<Listing>,
    lastSortingUsedPreference: Preference<Int>
  ): Flowable<Change.QueryModeUpdate> {
    val listingChange = actions.ofType(Action.SetListing::class)
      .flatMap { action ->
        val type = types.getOrNull(action.index)
        if (type == null) {
          // Do nothing if the index is out of bounds.
          Flowable.empty()
        } else {
          // Save this listing as the last used.
          lastSortingUsedPreference.set(action.index)

          // Emit an update to listing mode.
          val queryMode = QueryMode.List(type)
          Flowable.just(Change.QueryModeUpdate(queryMode))
        }
      }

    val filterChange = actions.ofType(Action.SetFilters::class)
      .flatMap { action ->
        // Get the filters to apply, update their inner value and ignore the ones with the default
        // value.
        val filters = action.filters
          .onEach { it.updateInnerValue() }
          .map { it.filter }
          .filter { !it.isDefaultValue() }

        if (filters.isEmpty()) {
          // Do nothing if there are no filters to apply.
          Flowable.empty()
        } else {
          // Emit an update to search/filter mode.
          val queryMode = QueryMode.Filter(filters)
          Flowable.just(Change.QueryModeUpdate(queryMode))
        }
      }

    return Flowable.merge(listingChange, filterChange)
  }

  /**
   * Returns the page loader for this [source] given a [queryMode]. Whenever the query mode changes,
   * this method is called again and the previous one is unsubscribed.
   */
  private fun bindPageLoader(
    source: CatalogSource,
    queryMode: QueryMode,
    mangaInitializerSubject: FlowableProcessor<List<Manga>>
  ): Flowable<Change> {
    val currentPage = AtomicInteger(1)
    val hasNextPage = AtomicBoolean(true)

    return actions.ofType(Action.LoadMore::class)
      .startWith(Action.LoadMore) // Always load the initial page
      .map { currentPage.get() }
      .concatMap f@{ requestedPage ->
        val page = currentPage.get()
        if (!hasNextPage.get() || requestedPage < page) return@f Flowable.empty<Change>()

        val mangasPageSingle = when (queryMode) {
          is QueryMode.Filter ->
            searchMangaPageFromCatalogSource.interact(source, queryMode.filters, page)
          is QueryMode.List ->
            listMangaPageFromCatalogSource.interact(source, queryMode.listing, page)
        }

        mangasPageSingle.subscribeOn(Schedulers.io())
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

  /**
   * Returns the changes of the manga that are initialized.
   */
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

  /**
   * Returns the changes of display mode updates.
   */
  private fun bindDisplayMode(gridPreference: Preference<Boolean>): Flowable<Change> {
    return actions.ofType(Action.SwapDisplayMode::class)
      .map {
        val newValue = !gridPreference.get()
        gridPreference.set(newValue)
        newValue
      }
      .map(Change::DisplayModeUpdate)
  }

  /**
   * Returns the changes of errors occurred when requesting pages.
   */
  private fun bindErrorDelivered(): Flowable<Change> {
    return actions.ofType(Action.ErrorDelivered::class)
      .map { Change.LoadingError(null) }
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
    actions.emit(Action.SwapDisplayMode)
  }

  /**
   * Emits an action to request the next page of the catalog.
   */
  fun loadMore() {
    actions.emit(Action.LoadMore)
  }

  /**
   * Emits an action to query the catalog with the listing at the given [index].
   */
  fun setListing(index: Int) {
    actions.emit(Action.SetListing(index))
  }

  /**
   * Emits an action to query the catalog with the given [filters].
   */
  fun setFilters(filters: List<FilterWrapper<*>>) {
    actions.emit(Action.SetFilters(filters))
  }

}

/**
 * List of actions that can be used to request a [Change] and mutate the view state.
 *
 * [SwapDisplayMode] is used to change the layout manager to a grid or a list.
 * [LoadMore] is used to request the next page of the catalog's current query.
 * [SetListing] is used to set a new query on the catalog with the given listing.
 * [SetFilters] is used to set a new query on the catalog with the given filters.
 * [ErrorDelivered] is used to notify the presenter that the UI has received the error.
 */
private sealed class Action {

  object SwapDisplayMode : Action()
  object LoadMore : Action()
  data class SetListing(val index: Int) : Action()
  data class SetFilters(val filters: List<FilterWrapper<*>>) : Action()
  object ErrorDelivered : Action()

}

/**
 * List of changes that can produce a new view state.
 *
 * [QueryModeUpdate] sets a new query mode. It's the result of applying a [SetListing] or
 *   [SetFilters] action.
 * [PageReceived] adds a page received from the catalog to the current list.
 * [DisplayModeUpdate] sets the new display mode. It's the result of applying a [SwapDisplayMode]
 *   action.
 * [MangaInitialized] replaces the initialized manga with the non-initialized on the current list.
 * [Loading] sets the loading state, and also sets an empty list of manga if it's the first page.
 * [LoadingError] sets the error that can occur when the requested page fails to load.
 */
private sealed class Change {

  data class QueryModeUpdate(val mode: QueryMode) : Change()
  data class PageReceived(val page: MangasPage) : Change()
  data class DisplayModeUpdate(val isGridMode: Boolean) : Change()
  data class MangaInitialized(val manga: Manga) : Change()
  data class Loading(val isLoading: Boolean, val page: Int) : Change()
  data class LoadingError(val error: Throwable?) : Change()

}

/**
 * Function that reduces a [change] into a new [CatalogBrowseViewState] given the current [state].
 * The resulting view state is later emitted through a [BehaviorProcessor] from which the UI
 * receives the updates.
 */
private fun reduce(state: CatalogBrowseViewState, change: Change): CatalogBrowseViewState {
  return when (change) {
    is Change.QueryModeUpdate -> state.copy(queryMode = change.mode)
    is Change.PageReceived -> state.copy(
      mangas = state.mangas + change.page.mangas,
      isLoading = false,
      hasMorePages = change.page.hasNextPage
    )
    is Change.DisplayModeUpdate -> state.copy(isGridMode = change.isGridMode)
    is Change.MangaInitialized -> state.copy(mangas = state.mangas
      .replaceFirst({ it.id == change.manga.id }, change.manga)
    )
    is Change.Loading -> state.copy(
      isLoading = change.isLoading,
      hasMorePages = true,
      mangas = if (change.isLoading && change.page == 1) emptyList() else state.mangas
    )
    is Change.LoadingError -> state.copy(error = change.error, isLoading = false)
  }
}
