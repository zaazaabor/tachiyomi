package tachiyomi.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tachiyomi.app.R
import tachiyomi.ui.base.MvpScopedController

class LibraryController : MvpScopedController<LibraryPresenter>() {

  override fun getPresenterClass() = LibraryPresenter::class.java

  override fun getModule() = LibraryModule(this)

  override fun getTitle() = resources?.getString(R.string.label_library)

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    presenter // TODO forcing presenter creation
    return View(container.context) // TODO
  }

}
