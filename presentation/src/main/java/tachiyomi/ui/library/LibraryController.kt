package tachiyomi.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tachiyomi.applicationScope
import tachiyomi.ui.base.MvpController
import toothpick.Toothpick

class LibraryController : MvpController<LibraryPresenter>() {

  private val scope = applicationScope(this).also {
    it.installModules(LibraryModule(this))
  }

  override fun createPresenter(): LibraryPresenter {
    return scope.getInstance(LibraryPresenter::class.java)
  }

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

  override fun onDestroy() {
    Toothpick.closeScope(this)
    super.onDestroy()
  }

}
