package tachiyomi.ui.catalogbrowse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tachiyomi.ui.base.MvpScopedController

class CatalogBrowseController(
  bundle: Bundle? = null
) : MvpScopedController<CatalogBrowsePresenter>(bundle) {

  constructor(sourceId: Long) : this(Bundle().apply {
    putLong(SOURCE_KEY, sourceId)
  })

  fun getSourceId() = args.getLong(SOURCE_KEY)

  override fun getPresenterClass() = CatalogBrowsePresenter::class.java

  override fun getModule() = CatalogBrowseModule(this)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return View(container.context) // TODO
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)
  }

  private companion object {
    const val SOURCE_KEY = "source_id"
  }

}
