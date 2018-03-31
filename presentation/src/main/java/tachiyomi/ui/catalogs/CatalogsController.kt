package tachiyomi.ui.catalogs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.catalogue_controller.*
import tachiyomi.app.R
import tachiyomi.domain.source.CatalogueSource
import tachiyomi.ui.base.MvpScopedController
import tachiyomi.ui.base.withFadeTransaction
import tachiyomi.ui.catalogbrowse.CatalogBrowseController

class CatalogsController : MvpScopedController<CatalogsPresenter>(),
  CatalogsAdapter.Listener {

  private var adapter: CatalogsAdapter? = null

  override fun getPresenterClass() = CatalogsPresenter::class.java

  override fun getTitle() = resources?.getString(R.string.label_catalogues)

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.catalogue_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)
    adapter = CatalogsAdapter(this)
    catalogue_recycler.layoutManager = LinearLayoutManager(view.context)
    catalogue_recycler.adapter = adapter

    presenter.stateRelay
      .map { it.catalogues }
      .distinctUntilChanged()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeWithView(::renderCatalogues)
  }

  override fun onDestroyView(view: View) {
    adapter = null
    super.onDestroyView(view)
  }

  //===========================================================================
  // ~ Render
  //===========================================================================

  private fun renderCatalogues(catalogues: List<CatalogueSource>) {
    adapter?.submitList(catalogues)
  }

  //===========================================================================
  // ~ User interaction
  //===========================================================================

  override fun onRowClick(catalogue: CatalogueSource) {
    router.pushController(CatalogBrowseController(catalogue.id).withFadeTransaction())
  }

  override fun onBrowseClick(catalogue: CatalogueSource) {
    onRowClick(catalogue)
  }

  override fun onLatestClick(catalogue: CatalogueSource) {
  }

}
