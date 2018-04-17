package tachiyomi.ui.manga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import tachiyomi.app.R
import tachiyomi.ui.base.BaseController

class MangaController : BaseController() {

  init {
    setHasOptionsMenu(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return View(container.context)
  }

  override fun getTitle(): String? {
    return "Manga"
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.manga, menu)
  }

  override fun onChangeStarted(
    changeHandler: ControllerChangeHandler,
    changeType: ControllerChangeType
  ) {
    super.onChangeStarted(changeHandler, changeType)
    setOptionsMenuHidden(!changeType.isEnter)
  }
}
