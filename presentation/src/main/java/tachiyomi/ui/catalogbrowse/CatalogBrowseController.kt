package tachiyomi.ui.catalogbrowse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.catalogbrowse_controller.*
import kotlinx.android.synthetic.main.catalogbrowse_filters_sheet.view.*
import tachiyomi.app.R
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Sorting
import tachiyomi.ui.base.MvpScopedController
import tachiyomi.ui.base.withFadeTransition
import tachiyomi.ui.manga.MangaController
import tachiyomi.util.visibleIf
import tachiyomi.widget.EndlessRecyclerViewScrollListener

class CatalogBrowseController(
  bundle: Bundle? = null
) : MvpScopedController<CatalogBrowsePresenter>(bundle),
  CatalogBrowseAdapter.Listener,
  EndlessRecyclerViewScrollListener.Callback {

  private var adapter: CatalogBrowseAdapter? = null

  private var filtersAdapter: RecyclerView.Adapter<*>? = null

  constructor(sourceId: Long) : this(Bundle().apply {
    putLong(SOURCE_KEY, sourceId)
  })

  //===========================================================================
  // ~ Presenter
  //===========================================================================

  override fun getPresenterClass() = CatalogBrowsePresenter::class.java

  override fun getModule() = CatalogBrowseModule(this)

  fun getSourceId() = args.getLong(SOURCE_KEY)

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.catalogbrowse_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    adapter = CatalogBrowseAdapter(this)

    catalogbrowse_recycler.setHasFixedSize(true)

    RxToolbar.navigationClicks(catalogbrowse_toolbar)
      .subscribeWithView { router.handleBack() }

    catalogbrowse_toolbar.inflateMenu(R.menu.catalogbrowse_menu)
    RxToolbar.itemClicks(catalogbrowse_toolbar)
      .subscribeWithView { item ->
        if (item.groupId == GROUP_SORT) {
          setSorting(item.order)
        } else when (item.itemId) {
          R.id.action_display_mode -> swapDisplayMode()
          else -> super.onOptionsItemSelected(item)
        }
      }

    // Initialize search menu
//    val searchItem = catalogbrowse_toolbar.menu.findItem(R.id.action_search)
//    val searchView = searchItem.actionView as SearchView
//    searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
//      override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
//        for (i in 0 until menu.size()) {
//          menu.getItem(i).isVisible = false
//        }
//        return true
//      }
//
//      override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
//        setQuery("", true)
//        // Ugly solution, but items are lost when the view is collapsed
//        //activity?.invalidateOptionsMenu()
//        return true
//      }
//    })
//
//    val initialText = querySubject
//      .take(1)
//      .doOnNext { query ->
//        if (query.isNotBlank()) {
//          searchItem.expandActionView()
//          searchView.clearFocus()
//          searchView.setQuery(query, true)
//        }
//      }
//

    filtersAdapter = FiltersAdapter()

//    searchView.queryTextChangeEvents()
//      .skipInitialValue()
//      .filter { it.isSubmitted }
//      .subscribeWithView { setQuery(it.queryText().toString()) }

    catalogbrowse_fab.clicks()
      .subscribeWithView {
        val dialog = FiltersBottomSheetDialog(view.context)

        val filtersView = LayoutInflater.from(view.context)
          .inflate(R.layout.catalogbrowse_filters_sheet, null)

        val recycler = filtersView.catalogbrowse_filters_recycler

        recycler.layoutManager = FlexboxLayoutManager(view.context)
        recycler.adapter = filtersAdapter
        dialog.setContentView(filtersView)
        dialog.show()
      }

    presenter.stateRelay
      .scanWithPrevious()
      .subscribeWithView { (state, prevState) -> dispatch(state, prevState) }
  }

  override fun onDestroyView(view: View) {
    adapter = null
    filtersAdapter = null
    super.onDestroyView(view)
  }

  //===========================================================================
  // ~ Render
  //===========================================================================

  private fun dispatch(state: CatalogBrowseViewState, prevState: CatalogBrowseViewState?) {
    if (state.source != null && state.source != prevState?.source) {
      renderSource(state.source)
    }
    if (state.isGridMode != prevState?.isGridMode) {
      renderLayoutManager(state.isGridMode)
      renderDisplayMode(state.isGridMode)
    }
    if (state.sortings != prevState?.sortings) {
      renderSortings(state.sortings)
    }
    if (state.activeSorting != prevState?.activeSorting) {
      renderActiveSorting(state.activeSorting)
    }
//    if (state.query != prevState?.query) {
//      renderQuery(state.query)
//    }
    if (state.isLoading != prevState?.isLoading) {
      renderLoading(state.isLoading, state.mangas)
    }
    if (state.mangas !== prevState?.mangas || state.isLoading != prevState.isLoading
      || state.hasMorePages != prevState.hasMorePages) {

      renderList(state.mangas, state.isLoading, state.hasMorePages)
    }
  }

  private fun renderSource(source: CatalogSource) {
    catalogbrowse_toolbar.title = source.name
  }

  private fun renderSortings(sortings: List<Sorting>) {
    val sortItem = catalogbrowse_toolbar.menu.findItem(R.id.action_sort)
    val sortMenu = sortItem.subMenu
    sortItem.isVisible = sortings.isNotEmpty()
    sortMenu.clear()
    sortings.forEachIndexed { index, type ->
      sortMenu.add(GROUP_SORT, Menu.NONE, index, type.name)
    }
    sortMenu.setGroupCheckable(GROUP_SORT, true, true)
  }

  private fun renderActiveSorting(sorting: Sorting?) {
    catalogbrowse_toolbar.subtitle = sorting?.name ?: ""
    val sortMenu = catalogbrowse_toolbar.menu.findItem(R.id.action_sort).subMenu
    for (i in 0 until sortMenu.size()) {
      val subItem = sortMenu.getItem(i)
      if (subItem.title == sorting?.name) {
        subItem.isChecked = true
      }
    }
  }

  private fun renderLayoutManager(isGridMode: Boolean) {
    while (catalogbrowse_recycler.itemDecorationCount > 0) {
      catalogbrowse_recycler.removeItemDecorationAt(0)
    }
    catalogbrowse_recycler.clearOnScrollListeners()

    val prevLayoutState = catalogbrowse_recycler.layoutManager?.onSaveInstanceState()

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
    val endlessScroll = EndlessRecyclerViewScrollListener(layoutManager, this)

    catalogbrowse_recycler.layoutManager = layoutManager
    catalogbrowse_recycler.addOnScrollListener(endlessScroll)
    if (prevLayoutState != null) {
      layoutManager.onRestoreInstanceState(prevLayoutState)
    }
    catalogbrowse_recycler.adapter = adapter
  }

  private fun renderDisplayMode(isGridMode: Boolean) {
    val icon = if (isGridMode) {
      R.drawable.ic_view_list_white_24dp
    } else {
      R.drawable.ic_view_module_white_24dp
    }
    catalogbrowse_toolbar.menu.findItem(R.id.action_display_mode).setIcon(icon)
  }

  private fun renderQuery(query: String) {
    // TODO
  }

  private fun renderLoading(loading: Boolean, mangas: List<Manga>) {
    catalogbrowse_progress.visibleIf { loading && mangas.isEmpty() }
  }

  private fun renderList(mangas: List<Manga>, isLoading: Boolean, hasMorePages: Boolean) {
    adapter?.submitList(mangas, isLoading, !hasMorePages)
  }

  //===========================================================================
  // ~ User actions
  //===========================================================================

  private fun swapDisplayMode() {
    presenter.swapDisplayMode()
  }

  private fun setSorting(index: Int) {
    presenter.setSorting(index)
  }

  private fun setQuery(query: String) {
    presenter.setQuery(query)
  }

  override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
    presenter.loadMore()
  }

  override fun onMangaClick(manga: Manga) {
    findRootRouter().pushController(MangaController(manga.id).withFadeTransition())
  }

  private companion object {
    const val SOURCE_KEY = "source_id"
    const val GROUP_SORT = 1
  }

}
