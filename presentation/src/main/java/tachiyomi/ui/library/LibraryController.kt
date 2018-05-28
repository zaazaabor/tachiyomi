package tachiyomi.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.support.v7.widget.navigationClicks
import kotlinx.android.synthetic.main.library_controller.*
import tachiyomi.app.R
import tachiyomi.ui.base.MvpScopedController
import tachiyomi.ui.home.HomeController

class LibraryController : MvpScopedController<LibraryPresenter>() {

  override fun getPresenterClass() = LibraryPresenter::class.java

  override fun getModule() = LibraryModule(this)

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.library_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)
    library_toolbar.navigationClicks()
      .subscribeWithView { (parentController as? HomeController)?.openDrawer() }
  }

}
