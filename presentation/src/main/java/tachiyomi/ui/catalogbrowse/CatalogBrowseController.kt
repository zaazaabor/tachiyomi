package tachiyomi.ui.catalogbrowse

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.DividerItemDecoration.VERTICAL
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.jakewharton.rxbinding2.support.v7.widget.queryTextChangeEvents
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.catalogbrowse_controller.*
import tachiyomi.app.R
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.CatalogSource
import tachiyomi.ui.base.MvpScopedController
import tachiyomi.ui.base.withFadeTransaction
import tachiyomi.ui.manga.MangaController
import tachiyomi.util.visibleIf
import tachiyomi.widget.EndlessRecyclerViewScrollListener

class CatalogBrowseController(
  bundle: Bundle? = null
) : MvpScopedController<CatalogBrowsePresenter>(bundle),
  CatalogBrowseAdapter.Listener,
  EndlessRecyclerViewScrollListener.Callback {

  private var adapter: CatalogBrowseAdapter? = null

  private var menuSubscription: Disposable? = null
  private var querySubject = BehaviorSubject.create<String>()
  private var gridModeSubject = BehaviorSubject.create<Boolean>()

  constructor(sourceId: Long) : this(Bundle().apply {
    putLong(SOURCE_KEY, sourceId)
  })

  init {
    setHasOptionsMenu(true)
  }

  //===========================================================================
  // ~ Presenter
  //===========================================================================

  override fun getPresenterClass() = CatalogBrowsePresenter::class.java

  override fun getModule() = CatalogBrowseModule(this)

  fun getSourceId() = args.getLong(SOURCE_KEY)

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onChangeStarted(
    changeHandler: ControllerChangeHandler,
    changeType: ControllerChangeType
  ) {
    super.onChangeStarted(changeHandler, changeType)

    if (changeType.isEnter) {

    } else {
      menuSubscription?.dispose()
      menuSubscription = null
    }

    setOptionsMenuHidden(!changeType.isEnter)
  }

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

    presenter.stateRelay
      .scanWithPrevious()
      .subscribeWithView { (state, prevState) -> dispatch(state, prevState) }
  }

  override fun onDestroyView(view: View) {
    adapter = null
    menuSubscription?.dispose()
    menuSubscription = null
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
    if (state.query != prevState?.query) {
      renderQuery(state.query)
    }
    if (state.isLoading != prevState?.isLoading) {
      renderLoading(state.isLoading, state.mangas)
    }
    if (state.hasMorePages != prevState?.hasMorePages) {
      renderHasMorePages(state.hasMorePages)
    }
    if (state.mangas !== prevState?.mangas) {
      renderMangas(state.mangas)
    }
  }

  private fun renderSource(source: CatalogSource) {
    requestTitle(source.name)
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
    gridModeSubject.onNext(isGridMode)
  }

  private fun renderQuery(query: String) {
    querySubject.onNext(query)
  }

  private fun renderLoading(loading: Boolean, mangas: List<Manga>) {
    catalogbrowse_progress.visibleIf { loading && mangas.isEmpty() }
    adapter?.setLoading(loading && mangas.isNotEmpty())
  }

  private fun renderHasMorePages(hasMorePages: Boolean) {
    adapter?.setEndReached(!hasMorePages)
  }

  private fun renderMangas(mangas: List<Manga>) {
    adapter?.submitList(mangas)
  }

  //===========================================================================
  // ~ Options menu
  //===========================================================================

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.catalogbrowse_menu, menu)

    // Dispose of previous subscription if needed
    menuSubscription?.dispose()

    // Initialize search menu
    val searchItem = menu.findItem(R.id.action_search)
    val searchView = searchItem.actionView as SearchView
    val displayItem = menu.findItem(R.id.action_display_mode)

    searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
      override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
        for (i in 0 until menu.size()) {
          menu.getItem(i).isVisible = false
        }
        return true
      }

      override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        setQuery("", true)
        // Ugly solution, but items are lost when the view is collapsed
        activity?.invalidateOptionsMenu()
        return true
      }
    })

    val listModeUpdates = gridModeSubject
      .doOnNext { isGridMode ->
        val icon = if (isGridMode) {
          R.drawable.ic_view_list_white_24dp
        } else {
          R.drawable.ic_view_module_white_24dp
        }
        displayItem.setIcon(icon)
      }

    val initialText = querySubject
      .take(1)
      .doOnNext { query ->
        if (query.isNotBlank()) {
          searchItem.expandActionView()
          searchView.clearFocus()
          searchView.setQuery(query, true)
        }
      }

    val textChanges = searchView.queryTextChangeEvents()
      .skipInitialValue()
      .doOnNext { setQuery(it.queryText().toString(), it.isSubmitted) }

    menuSubscription = Observable.merge(initialText, textChanges, listModeUpdates)
      .subscribe()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_display_mode -> swapDisplayMode()
      else -> return super.onOptionsItemSelected(item)
    }
    return true
  }

  //===========================================================================
  // ~ User actions
  //===========================================================================

  private fun swapDisplayMode() {
    presenter.swapDisplayMode()
  }

  private fun setQuery(query: String, submit: Boolean) {
    presenter.setQuery(query, submit)
  }

  override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
    presenter.loadMore()
  }

  override fun onMangaClick(manga: Manga) {
    router.pushController(MangaController().withFadeTransaction())
  }

  private companion object {
    const val SOURCE_KEY = "source_id"
  }

}
