package tachiyomi.ui.catalogbrowse

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.DividerItemDecoration.VERTICAL
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.catalogbrowse_controller.*
import tachiyomi.app.R
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.CatalogueSource
import tachiyomi.ui.base.MvpScopedController
import tachiyomi.util.visibleIf

class CatalogBrowseController(
  bundle: Bundle? = null
) : MvpScopedController<CatalogBrowsePresenter>(bundle) {

  private var adapter: CatalogBrowseAdapter? = null

  constructor(sourceId: Long) : this(Bundle().apply {
    putLong(SOURCE_KEY, sourceId)
  })

  fun getSourceId() = args.getLong(SOURCE_KEY)

  override fun getPresenterClass() = CatalogBrowsePresenter::class.java

  override fun getModule() = CatalogBrowseModule(this)

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

    presenter.stateRelay
      .scanWithPrevious()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeWithView { (state, prevState) -> dispatch(state, prevState) }
  }

  override fun onDestroyView(view: View) {
    adapter = null
    super.onDestroyView(view)
  }

  //===========================================================================
  // ~ Render
  //===========================================================================

  private fun dispatch(state: CatalogBrowseViewState, prevState: CatalogBrowseViewState?) {
    if (state.source != null && state.source != prevState?.source) {
      renderSource(state.source)
    }
    if (state.isListMode != prevState?.isListMode) {
      renderLayoutManager(state.isListMode)
    }
    if (state.mangas !== prevState?.mangas) {
      renderMangas(state.mangas)
    }
  }

  private fun renderSource(source: CatalogueSource) {
    setTitle(source.name)
  }

  private fun renderLayoutManager(isListMode: Boolean) {
    while (catalogbrowse_recycler.itemDecorationCount > 0) {
      catalogbrowse_recycler.removeItemDecorationAt(0)
    }

    catalogbrowse_recycler.layoutManager = if (isListMode) {
      catalogbrowse_recycler.addItemDecoration(DividerItemDecoration(activity, VERTICAL))
      LinearLayoutManager(activity)
    } else {
      GridLayoutManager(activity, 1)
    }
    catalogbrowse_recycler.adapter = adapter
  }

  private fun renderMangas(mangas: List<Manga>) {
    catalogbrowse_progress.visibleIf { mangas.isEmpty() }
    adapter?.submitList(mangas)
  }

  private companion object {
    const val SOURCE_KEY = "source_id"
  }

}
