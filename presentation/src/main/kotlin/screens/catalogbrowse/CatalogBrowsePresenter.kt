/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalogbrowse

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.data.catalog.prefs.CatalogPreferences
import tachiyomi.domain.manga.interactor.ListMangaPageFromCatalogSource
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.interactor.SearchMangaPageFromCatalogSource
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.ui.presenter.BasePresenter
import timber.log.Timber
import javax.inject.Inject
import tachiyomi.ui.screens.catalogbrowse.CatalogBrowseViewState as ViewState
import tachiyomi.ui.screens.catalogbrowse.CatalogBrowseAction as Action

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
   * Subject which allows emitting actions and subscribing to a specific one while supporting
   * backpressure.
   */
  private val actions = PublishRelay.create<Action>()

  /**
   * Behavior subject containing the last emitted view state.
   */
  private val state = BehaviorRelay.create<ViewState>()

  /**
   * State subject as a consumer-only observable.
   */
  val stateObserver: Observable<ViewState> = state

  /**
   * Last used listing preference.
   */
  private val lastListingPreference = catalogPreferences.lastListingUsed(params.sourceId)

  /**
   * Grid mode preference.
   */
  private val gridPreference = catalogPreferences.gridMode()

  init {
    actions
      .observeOn(schedulers.io)
      .reduxStore(
        initialState = getInitialViewState(),
        sideEffects = listOf(
          ::setListingSideEffect,
          ::setFiltersSideEffect,
          ::setDisplayModeSideEffect,
          ::loadNextSideEffect
        ),
        reducer = { state, action -> action.reduce(state) }
      )
      .distinctUntilChanged()
      .observeOn(schedulers.main)
      .subscribe(state::accept)
      .addTo(disposables)
  }

  private fun getInitialViewState(): ViewState {
    // Find the requested source or early return an initial state with a not found error.
    val source = sourceManager.get(params.sourceId) as? CatalogSource
      ?: return ViewState(error = Exception("Source not found"))

    // Get the listings of the source and the initial listing and query mode to set.
    val listings = source.getListings()
    val initialListing = listings.getOrNull(lastListingPreference.get()) ?: listings.firstOrNull()
    val initialQueryMode = QueryMode.List(initialListing)

    return ViewState(
      source = source,
      queryMode = initialQueryMode,
      listings = listings,
      filters = getWrappedFilters(source),
      isGridMode = gridPreference.get()
    )
  }

  private fun setListingSideEffect(
    actions: Observable<Action>,
    state: StateAccessor<ViewState>
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

  @Suppress("unused_parameter")
  private fun setFiltersSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
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
    stateFn: StateAccessor<ViewState>
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
              .map(Action::MangaInitialized)

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
    state: StateAccessor<ViewState>
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

