package tachiyomi.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import kotlinx.android.synthetic.main.library_controller.*
import tachiyomi.app.R
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.ui.base.MvpController
import tachiyomi.ui.home.HomeChildController
import tachiyomi.ui.home.HomeController

class LibraryController : MvpController<LibraryPresenter>(),
  HomeChildController {

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
    setupToolbarIconWithHomeController(library_toolbar)
    RxToolbar.navigationClicks(library_toolbar)
      .subscribeWithView { (parentController as? HomeController)?.openDrawer() }

    presenter.state
      .scanWithPrevious()
      .subscribeWithView { (state, prevState) -> render(state, prevState) }
  }

  private fun render(state: LibraryViewState, prevState: LibraryViewState?) {

  }

}
