package tachiyomi.ui.home

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import kotlinx.android.synthetic.main.home_controller.*
import tachiyomi.app.R
import tachiyomi.ui.base.BaseController
import tachiyomi.ui.base.withFadeTransition
import tachiyomi.ui.base.withoutTransition
import tachiyomi.ui.catalogs.CatalogsController
import tachiyomi.ui.library.LibraryController

class HomeController : BaseController() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.home_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    val router = getChildRouter(home_controller_container)

    home_nav_view.setNavigationItemSelectedListener { item ->
      val id = item.itemId

      val currentRoot = router.backstack.firstOrNull()
      if (currentRoot?.tag()?.toIntOrNull() != id) {
        when (id) {
          R.id.nav_drawer_library -> setRoot(router, LibraryController(), id)
          R.id.nav_drawer_catalogues -> setRoot(router, CatalogsController(), id)
        }
      }
      home_drawer.closeDrawer(GravityCompat.START)
      true
    }

    if (!router.hasRootController()) {
      setSelectedDrawerItem(R.id.nav_drawer_catalogues)
    }
  }

  override fun onDestroyView(view: View) {
    home_nav_view.setNavigationItemSelectedListener(null)
    super.onDestroyView(view)
  }

  private fun setRoot(router: Router, controller: Controller, id: Int) {
    val transaction = if (!router.hasRootController()) {
      controller.withoutTransition()
    } else {
      controller.withFadeTransition()
    }
    router.setRoot(transaction.tag("$id"))
  }

  private fun setSelectedDrawerItem(itemId: Int) {
    if (view == null) return
    home_nav_view.setCheckedItem(itemId)
    home_nav_view.menu.performIdentifierAction(itemId, 0)
  }

  fun openDrawer() {
    home_drawer?.openDrawer(Gravity.START)
  }

}
