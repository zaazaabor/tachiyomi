package tachiyomi.ui.home

import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.bluelinelabs.conductor.Controller
import com.google.android.material.floatingactionbutton.FloatingActionButton
import tachiyomi.app.R
import tachiyomi.util.getDrawableAttr

interface HomeChildController {

  interface FAB : HomeChildController {
    fun createFAB(container: ViewGroup): FloatingActionButton
  }

  fun Controller.setupToolbarIconWithHomeController(toolbar: Toolbar) {
    val homeCtrl = parentController as? HomeController ?: return
    val homeRouter = homeCtrl.childRouter ?: return

    toolbar.navigationIcon = if (homeRouter.backstackSize == 1) {
      if (homeCtrl.usesDrawer) {
        toolbar.context.getDrawable(R.drawable.ic_menu_white_24dp)
      } else {
        null
      }
    } else {
      toolbar.context.getDrawableAttr(android.R.attr.homeAsUpIndicator)
    }
  }

}
