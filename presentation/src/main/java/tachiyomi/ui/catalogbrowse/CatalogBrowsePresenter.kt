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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class CatalogBrowsePresenter @Inject constructor(
  private val sourceId: Long?,
  private val sourceManager: SourceManager, // TODO new use case to retrieve a catalogue?
  private val searchMangaPageFromCatalogSource: SearchMangaPageFromCatalogSource,
  private val listMangaPageFromCatalogSource: ListMangaPageFromCatalogSource,
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

    val listings = source.getListings()

    val gridPreference = catalogPreferences.gridMode()
    val lastListingPreference = catalogPreferences.lastListingUsed(sourceId)

    val initialListing = listings.getOrNull(lastListingPreference.get()) ?: listings.firstOrNull()
    val initialQueryMode = QueryMode.List(initialListing)

    currentState = CatalogBrowseViewState(
      source = source,
      queryMode = initialQueryMode,
      listings = listings,
      filters = getWrappedFilters(source),
      isGridMode = gridPreference.get()
    )

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

    return Flowable.merge(changes)
  }

  private fun bindQueryModeChange(
    types: List<Listing>,
    lastSortingUsedPreference: Preference<Int>
  ): Flowable<Change.QueryModeUpdate> {
    val listingChange = actions.ofType(Action.SetListing::class)
      .flatMap { action ->
        val type = types.getOrNull(action.index)
        if (type == null) {
          Flowable.empty()
        } else {
          lastSortingUsedPreference.set(action.index)
          val queryMode = QueryMode.List(type)
          Flowable.just(Change.QueryModeUpdate(queryMode))
        }
      }

    val filterChange = actions.ofType(Action.SetFilters::class)
      .flatMap { action ->
        val filters = action.filters
          .onEach { it.updateInnerValue() }
          .map { it.filter }
          .filter { !it.isDefaultValue() }

        if (filters.isEmpty()) {
          setListing(0) // TODO check
          Flowable.empty()
        } else {
          val queryMode = QueryMode.Filter(filters)
          Flowable.just(Change.QueryModeUpdate(queryMode))
        }
      }

    return Flowable.merge(listingChange, filterChange)
  }

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

  fun swapDisplayMode() {
    actions.emit(Action.SwapDisplayMode)
  }

  fun loadMore() {
    actions.emit(Action.LoadMore)
  }

  fun setListing(index: Int) {
    actions.emit(Action.SetListing(index))
  }

  fun setFilters(filters: List<FilterWrapper<*>>) {
    actions.emit(Action.SetFilters(filters))
  }

}

private sealed class Action {
  object SwapDisplayMode : Action()
  object LoadMore : Action()
  data class SetListing(val index: Int) : Action()
  data class SetFilters(val filters: List<FilterWrapper<*>>) : Action()
  object ErrorDelivered : Action()
}

private sealed class Change {
  data class QueryModeUpdate(val mode: QueryMode) : Change()
  data class PageReceived(val page: MangasPage) : Change()
  data class DisplayModeUpdate(val isGridMode: Boolean) : Change()
  data class MangaInitialized(val manga: Manga) : Change()
  data class Loading(val isLoading: Boolean, val page: Int) : Change()
  data class LoadingError(val error: Throwable?) : Change()
  object SourceNotFound : Change()
}

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
    is Change.SourceNotFound -> state.copy(error = Exception("Source not found"))
  }
}
