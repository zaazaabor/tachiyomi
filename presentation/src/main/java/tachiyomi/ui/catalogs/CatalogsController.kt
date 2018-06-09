package tachiyomi.ui.catalogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.catalogs_controller.*
import tachiyomi.app.R
import tachiyomi.source.CatalogSource
import tachiyomi.ui.base.MvpScopedController
import tachiyomi.ui.base.withFadeTransition
import tachiyomi.ui.catalogbrowse.CatalogBrowseController
import tachiyomi.ui.home.HomeController

class CatalogsController : MvpScopedController<CatalogsPresenter>(),
  CatalogsAdapter.Listener {

  private var adapter: CatalogsAdapter? = null

  override fun getPresenterClass() = CatalogsPresenter::class.java

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.catalogs_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)
    adapter = CatalogsAdapter(this)
    catalogs_recycler.layoutManager = LinearLayoutManager(view.context)
    catalogs_recycler.adapter = adapter

    RxToolbar.navigationClicks(catalogs_toolbar)
      .subscribeWithView { (parentController as? HomeController)?.openDrawer() }

    presenter.stateRelay
      .map { it.catalogs }
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

  private fun renderCatalogues(catalogs: List<CatalogSource>) {
    adapter?.submitList(catalogs)
  }

  //===========================================================================
  // ~ User interaction
  //===========================================================================

  override fun onRowClick(catalog: CatalogSource) {
    router.pushController(CatalogBrowseController(catalog.id).withFadeTransition())
  }

  override fun onBrowseClick(catalog: CatalogSource) {
    onRowClick(catalog)
  }

  override fun onLatestClick(catalog: CatalogSource) {
  }

}
