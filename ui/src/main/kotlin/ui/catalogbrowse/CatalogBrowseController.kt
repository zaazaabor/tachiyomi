/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogbrowse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import kotlinx.android.synthetic.main.catalogbrowse_controller.*
import kotlinx.android.synthetic.main.catalogbrowse_filters_sheet.view.*
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Listing
import tachiyomi.ui.R
import tachiyomi.ui.base.MvpController
import tachiyomi.ui.base.withFadeTransition
import tachiyomi.ui.home.HomeChildController
import tachiyomi.ui.manga.MangaController
import tachiyomi.util.visibleIf
import tachiyomi.widget.EndlessRecyclerViewScrollListener

/**
 * Controller to handle the UI and user events on the catalog. It follows the MVI pattern, using a
 * dispatcher that renders the updates made to [CatalogBrowseViewState]. User events are usually
 * delegated to [CatalogBrowsePresenter] which updates the view state and the dispatcher receives
 * this new state.
 */
class CatalogBrowseController(
  bundle: Bundle? = null
) : MvpController<CatalogBrowsePresenter>(bundle),
  CatalogBrowseAdapter.Listener,
  EndlessRecyclerViewScrollListener.Callback,
  HomeChildController.FAB {

  /**
   * Adapter containing the list of manga from the catalogue. This field is set to null when the
   * view is destroyed.
   */
  private var adapter: CatalogBrowseAdapter? = null

  /**
   * Adapter containing the list of filters for the selected [CatalogSource]. This field is set to
   * null when the view is destroyed.
   */
  private var filtersAdapter: FiltersAdapter? = null

  /**
   * Constructor that takes a [sourceId] as parameter.
   */
  constructor(sourceId: Long) : this(Bundle().apply {
    putLong(SOURCE_KEY, sourceId)
  })

  //===========================================================================
  // ~ Presenter
  //===========================================================================

  /**
   * Returns the presenter class used by this controller.
   */
  override fun getPresenterClass() = CatalogBrowsePresenter::class.java

  /**
   * Returns the module of this controller that provides the dependencies of the presenter.
   */
  override fun getModule() = CatalogBrowseModule(this)

  /**
   * Returns the source id stored in the [Bundle] of this controller.
   */
  fun getSourceId() = args.getLong(SOURCE_KEY)

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  /**
   * Called when the view of this controller is being created.
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.catalogbrowse_controller, container, false)
  }

  /**
   * Called when the view of this controller is created. It initializes the listeners and the
   * dispatcher of state updates.
   */
  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    catalogbrowse_recycler.setHasFixedSize(true)

    // Initialize manga adapter
    adapter = CatalogBrowseAdapter(this)

    // Setup back navigation
    setupToolbarIconWithHomeController(catalogbrowse_toolbar)
    RxToolbar.navigationClicks(catalogbrowse_toolbar)
      .subscribeWithView { router.handleBack() }

    // Initialize toolbar menu
    catalogbrowse_toolbar.inflateMenu(R.menu.catalogbrowse_menu)
    RxToolbar.itemClicks(catalogbrowse_toolbar)
      .subscribeWithView { item ->
        if (item.groupId == GROUP_LISTING && item.isVisible) {
          // A listing element was clicked. Visibility is also checked because there's an extra,
          // hidden element for advanced search mode. It's required in order to unselect all the
          // visible items of the radio group.
          setListing(item.order)
        } else when (item.itemId) {
          R.id.action_display_mode -> swapDisplayMode()
          else -> super.onOptionsItemSelected(item)
        }
      }

    // Initialize filters adapter
    filtersAdapter = FiltersAdapter()

    presenter.stateObserver
      .scanWithPrevious()
      .subscribeWithView { (state, prevState) -> dispatch(state, prevState) }
  }

  override fun createFAB(container: ViewGroup): FloatingActionButton {
    val inflater = LayoutInflater.from(container.context)
    val fab = inflater.inflate(R.layout.catalogbrowse_fab, container, false)
    fab.setOnClickListener { showFilters() }
    return fab as FloatingActionButton
  }

  /**
   * Called when the view of this controller is being destroyed. It frees any view related
   * component.
   */
  override fun onDestroyView(view: View) {
    adapter = null
    filtersAdapter = null
    super.onDestroyView(view)
  }

  //===========================================================================
  // ~ Render
  //===========================================================================

  /**
   * Dispatcher of [state] updates, [prevState] is also provided to compare with the previous
   * state and render only the components that have changed.
   */
  private fun dispatch(state: CatalogBrowseViewState, prevState: CatalogBrowseViewState?) {
    if (state.source != null && state.source != prevState?.source) {
      renderSource(state.source)
    }
    if (state.isGridMode != prevState?.isGridMode) {
      renderLayoutManager(state.isGridMode)
      renderDisplayMode(state.isGridMode)
    }
    if (state.listings !== prevState?.listings) {
      renderListings(state.listings)
    }
    if (state.filters !== prevState?.filters) {
      renderFilters(state.filters)
    }
    if (state.queryMode != prevState?.queryMode) {
      renderQueryMode(state.queryMode)
    }
    if (state.isLoading != prevState?.isLoading) {
      renderLoading(state.isLoading, state.mangas)
    }
    if (state.mangas !== prevState?.mangas || state.isLoading != prevState.isLoading
      || state.hasMorePages != prevState.hasMorePages) {

      renderList(state.mangas, state.isLoading, state.hasMorePages)
    }
  }

  /**
   * Renders the selected [source].
   */
  private fun renderSource(source: CatalogSource) {
    catalogbrowse_toolbar.title = source.name
  }

  /**
   * Renders the [listings] of the selected source.
   */
  private fun renderListings(listings: List<Listing>) {
    val sortItem = catalogbrowse_toolbar.menu.findItem(R.id.action_sort)
    val sortMenu = sortItem.subMenu

    // Hide the sort item if the source doesn't provide any listing
    sortItem.isVisible = listings.isNotEmpty()

    // Clear any previous items
    sortMenu.clear()

    // Add a new sub item for each listing.
    listings.forEachIndexed { index, type ->
      sortMenu.add(GROUP_LISTING, Menu.NONE, index, type.name)
    }

    // Add a hidden advanced search item to allow to unselect all the listings.
    val searchItem = sortMenu.add(GROUP_LISTING, Menu.NONE, listings.size, "Advanced search")
    searchItem.isVisible = false

    // Set checkability as a radio group.
    sortMenu.setGroupCheckable(GROUP_LISTING, true, true)
  }

  /**
   * Render the [filters] of the selected source.
   */
  private fun renderFilters(filters: List<FilterWrapper<*>>) {
    filtersAdapter?.updateItems(filters)
  }

  /**
   * Render the current [queryMode], which can be of type list or filter.
   */
  private fun renderQueryMode(queryMode: QueryMode?) {
    when (queryMode) {
      is QueryMode.List -> {
        // Set toolbar subtitle with the listing's name
        val sortMenu = catalogbrowse_toolbar.menu.findItem(R.id.action_sort).subMenu

        // Select the item that matches the active listing.
        catalogbrowse_toolbar.subtitle = queryMode.listing?.name ?: ""
        sortMenu.forEach { item ->
          if (item.title == queryMode.listing?.name) {
            item.isChecked = true
            return@forEach
          }
        }
      }
      is QueryMode.Filter -> {
        // Set toolbar subtitle to advanced search.
        catalogbrowse_toolbar.subtitle = resources?.getString(R.string.advanced_search)

        // Select the hidden advanced search item.
        val sortMenu = catalogbrowse_toolbar.menu.findItem(R.id.action_sort).subMenu
        sortMenu.children.lastOrNull()?.isChecked = true
      }
    }
  }

  /**
   * Renders the layout manager of the manga list following the value of [isGridMode].
   */
  private fun renderLayoutManager(isGridMode: Boolean) {
    // Remove any previous item decorators and scroll listeners.
    while (catalogbrowse_recycler.itemDecorationCount > 0) {
      catalogbrowse_recycler.removeItemDecorationAt(0)
    }
    catalogbrowse_recycler.clearOnScrollListeners()

    // Save the state of the current layout manager, or null if none was set yet.
    val prevLayoutState = catalogbrowse_recycler.layoutManager?.onSaveInstanceState()

    // Create a new list or grid layout manager.
    val layoutManager = if (isGridMode) {
      GridLayoutManager(activity, 2).apply {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
          override fun getSpanSize(position: Int): Int {
            return adapter?.getSpanSize(position) ?: spanCount
          }
        }
      }
    } else {
      catalogbrowse_recycler.addItemDecoration(DividerItemDecoration(activity, VERTICAL))
      LinearLayoutManager(activity)
    }

    // Create a new scroll listener.
    val endlessScroll = EndlessRecyclerViewScrollListener(layoutManager, this)

    // Apply the new layout manager, scroll listener and restore the saved state.
    catalogbrowse_recycler.layoutManager = layoutManager
    catalogbrowse_recycler.addOnScrollListener(endlessScroll)
    if (prevLayoutState != null) {
      layoutManager.onRestoreInstanceState(prevLayoutState)
    }

    // Reset the adapter to recreate views and holders.
    catalogbrowse_recycler.adapter = adapter
  }

  /**
   * Renders the current display mode following the value of [isGridMode].
   */
  private fun renderDisplayMode(isGridMode: Boolean) {
    val icon = if (isGridMode) {
      R.drawable.ic_view_list_white_24dp
    } else {
      R.drawable.ic_view_module_white_24dp
    }
    catalogbrowse_toolbar.menu.findItem(R.id.action_display_mode).setIcon(icon)
  }

  /**
   * Renders a [ProgressBar] if it's [loading] and [mangas] is empty.
   */
  private fun renderLoading(loading: Boolean, mangas: List<Manga>) {
    catalogbrowse_progress.visibleIf { loading && mangas.isEmpty() }
  }

  /**
   * Renders the list of [mangas] and the [isLoading]/[hasMorePages] footer.
   */
  private fun renderList(mangas: List<Manga>, isLoading: Boolean, hasMorePages: Boolean) {
    adapter?.submitList(mangas, isLoading, !hasMorePages)
  }

  //===========================================================================
  // ~ User actions
  //===========================================================================

  /**
   * Swaps the display mode to list or grid.
   */
  private fun swapDisplayMode() {
    presenter.swapDisplayMode()
  }

  /**
   * Selects the source's listing at the given [index].
   */
  private fun setListing(index: Int) {
    presenter.setListing(index)
  }

  /**
   * Opens a bottom sheet with the list of filters for the selected source.
   */
  private fun showFilters() {
    val view = view ?: return

    val dialog = FiltersBottomSheetDialog(view.context)

    val filtersView = LayoutInflater.from(view.context)
      .inflate(R.layout.catalogbrowse_filters_sheet, null)

    filtersView.filter_button_close.setOnClickListener { dialog.cancel() }
    filtersView.filter_button_reset.setOnClickListener { resetFilters() }
    filtersView.filter_button_search.setOnClickListener { applyFilters() }

    val recycler = filtersView.catalogbrowse_filters_recycler

    recycler.layoutManager = FlexboxLayoutManager(view.context)
    recycler.adapter = filtersAdapter
    dialog.setContentView(filtersView)
    dialog.show()
  }

  /**
   * Sets all the filters to their initial value.
   */
  private fun resetFilters() {
    val adapter = filtersAdapter ?: return
    adapter.items.forEach { it.reset() }
    adapter.notifyDataSetChanged()
  }

  /**
   * Applies the current filters, also creating a new query mode.
   */
  private fun applyFilters() {
    val adapter = filtersAdapter ?: return
    presenter.setFilters(adapter.items)
  }

  /**
   * Called from the scroll listener when the end of the recycler view is reached. Used to
   * requests more items.
   */
  override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
    presenter.loadMore()
  }

  /**
   * Called from the adapter's listener when a [manga] is clicked.
   */
  override fun onMangaClick(manga: Manga) {
    findRootRouter().pushController(MangaController(manga.id).withFadeTransition())
  }

  private companion object {
    /**
     * Key used to store the source id of the selected catalog on a [Bundle].
     */
    const val SOURCE_KEY = "source_id"

    /**
     * Group id used for the listings on the toolbar's menu.
     */
    const val GROUP_LISTING = 1
  }

}
