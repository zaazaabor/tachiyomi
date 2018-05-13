package tachiyomi.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.drawable.DrawerArrowDrawable
import android.view.ViewGroup
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.android.synthetic.main.main_activity.*
import tachiyomi.app.R
import tachiyomi.ui.base.TabbedController
import tachiyomi.ui.base.withFadeTransaction
import tachiyomi.ui.catalogs.CatalogsController
import tachiyomi.ui.deeplink.ChapterDeepLinkController
import tachiyomi.ui.deeplink.MangaDeepLinkController
import tachiyomi.ui.library.LibraryController
import tachiyomi.ui.manga.MangaController
import tachiyomi.widget.TabsAnimator

class MainActivity : AppCompatActivity() {

  private lateinit var router: Router

  private var drawerArrow: DrawerArrowDrawable? = null

  private lateinit var tabAnimator: TabsAnimator

  private val startScreenId by lazy {
    when (1) { // TODO
      2 -> R.id.nav_drawer_recently_read
      3 -> R.id.nav_drawer_recent_updates
      else -> R.id.nav_drawer_library
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Do not let the launcher create a new activity http://stackoverflow.com/questions/16283079
    if (!isTaskRoot) {
      finish()
      return
    }

    // Init view
    setContentView(R.layout.main_activity)
    setSupportActionBar(toolbar)

    // Init conductor
    router = Conductor.attachRouter(this, controller_container, savedInstanceState)

    // Set toolbar nav icon
    drawerArrow = DrawerArrowDrawable(this)
    drawerArrow?.color = Color.WHITE
    toolbar.navigationIcon = drawerArrow

    tabAnimator = TabsAnimator(tabs)

    toolbar.setNavigationOnClickListener {
      if (router.backstackSize <= 1) {
        drawer.openDrawer(GravityCompat.START)
      } else {
        onBackPressed()
      }
    }

    nav_view.setNavigationItemSelectedListener { item ->
      val id = item.itemId

      val currentRoot = router.backstack.firstOrNull()
      if (currentRoot?.tag()?.toIntOrNull() != id) {
        when (id) {
          R.id.nav_drawer_library -> setRoot(LibraryController(), id)
//          R.id.nav_drawer_recent_updates -> setRoot(RecentChaptersController(), id)
//          R.id.nav_drawer_recently_read -> setRoot(RecentlyReadController(), id)
          R.id.nav_drawer_catalogues -> setRoot(CatalogsController(), id)
//          R.id.nav_drawer_extensions -> setRoot(ExtensionController(), id)
//          R.id.nav_drawer_downloads -> {
//            router.pushController(DownloadController().withFadeTransaction())
//          }
//          R.id.nav_drawer_settings -> {
//            router.pushController(SettingsMainController().withFadeTransaction())
//          }
        }
      }
      drawer.closeDrawer(GravityCompat.START)
      true
    }

    if (!router.hasRootController()) {
      // Set start screen
      if (!handleIntentAction(intent)) {
        setSelectedDrawerItem(R.id.nav_drawer_catalogues)
      }
    }
    router.addChangeListener(object : ControllerChangeHandler.ControllerChangeListener {
      override fun onChangeStarted(
        to: Controller?, from: Controller?, isPush: Boolean,
        container: ViewGroup, handler: ControllerChangeHandler
      ) {
        syncActivityViewWithController(to, from)
      }

      override fun onChangeCompleted(
        to: Controller?, from: Controller?, isPush: Boolean,
        container: ViewGroup, handler: ControllerChangeHandler
      ) {

      }
    })
  }

  override fun onDestroy() {
    nav_view.setNavigationItemSelectedListener(null)
    toolbar.setNavigationOnClickListener(null)
    super.onDestroy()
  }

  private fun setRoot(controller: Controller, id: Int) {
    router.setRoot(controller.withFadeTransaction().tag(id.toString()))
  }

  private fun setSelectedDrawerItem(itemId: Int) {
    if (!isFinishing) {
      nav_view.setCheckedItem(itemId)
      nav_view.menu.performIdentifierAction(itemId, 0)
    }
  }

  override fun onNewIntent(intent: Intent) {
    if (!handleIntentAction(intent)) {
      super.onNewIntent(intent)
    }
  }

  private fun handleIntentAction(intent: Intent): Boolean {
    when (intent.action) {
      SHORTCUT_LIBRARY -> setSelectedDrawerItem(R.id.nav_drawer_library)
      SHORTCUT_RECENTLY_UPDATED -> setSelectedDrawerItem(R.id.nav_drawer_recent_updates)
      SHORTCUT_RECENTLY_READ -> setSelectedDrawerItem(R.id.nav_drawer_recently_read)
      SHORTCUT_CATALOGUES -> setSelectedDrawerItem(R.id.nav_drawer_catalogues)
      SHORTCUT_MANGA -> {
        val extras = intent.extras ?: return false
        router.setRoot(RouterTransaction.with(MangaController(extras)))
      }
      SHORTCUT_DEEPLINK_MANGA -> {
        val extras = intent.extras ?: return false
        router.setRoot(RouterTransaction.with(MangaDeepLinkController(extras)))
      }
      SHORTCUT_DEEPLINK_CHAPTER -> {
        val extras = intent.extras ?: return false
        router.setRoot(RouterTransaction.with(ChapterDeepLinkController(extras)))
      }
//      SHORTCUT_DOWNLOADS -> {
//        if (router.backstack.none { it.controller() is DownloadController }) {
//          setSelectedDrawerItem(R.id.nav_drawer_downloads)
//        }
//      }
      else -> return false
    }
    return true
  }

  override fun onBackPressed() {
    val backstackSize = router.backstackSize
    if (drawer.isDrawerOpen(GravityCompat.START) || drawer.isDrawerOpen(GravityCompat.END)) {
      drawer.closeDrawers()
    } else if (backstackSize == 1 && router.getControllerWithTag("$startScreenId") == null) {
      setSelectedDrawerItem(startScreenId)
    } else if (backstackSize == 1 || !router.handleBack()) {
      super.onBackPressed()
    }
  }

  @SuppressLint("ObjectAnimatorBinding")
  private fun syncActivityViewWithController(to: Controller?, from: Controller?) {
//    if (from is DialogController || to is DialogController) {
//      return
//    }

    val showHamburger = router.backstackSize == 1
    if (showHamburger) {
      drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    } else {
      drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    ObjectAnimator.ofFloat(drawerArrow, "progress", if (showHamburger) 0f else 1f).start()

    if (from is TabbedController) {
      from.cleanupTabs(tabs)
    }
    if (to is TabbedController) {
      tabAnimator.expand()
      to.configureTabs(tabs)
    } else {
      tabAnimator.collapse()
      tabs.setupWithViewPager(null)
    }
  }

  companion object {
    // Shortcut actions
    const val SHORTCUT_LIBRARY = "eu.kanade.tachiyomi.SHOW_LIBRARY"
    const val SHORTCUT_RECENTLY_UPDATED = "eu.kanade.tachiyomi.SHOW_RECENTLY_UPDATED"
    const val SHORTCUT_RECENTLY_READ = "eu.kanade.tachiyomi.SHOW_RECENTLY_READ"
    const val SHORTCUT_CATALOGUES = "eu.kanade.tachiyomi.SHOW_CATALOGUES"
    const val SHORTCUT_DOWNLOADS = "eu.kanade.tachiyomi.SHOW_DOWNLOADS"
    const val SHORTCUT_MANGA = "eu.kanade.tachiyomi.SHOW_MANGA"
    const val SHORTCUT_DEEPLINK_MANGA = "eu.kanade.tachiyomi.OPEN_MANGA_LINK"
    const val SHORTCUT_DEEPLINK_CHAPTER = "eu.kanade.tachiyomi.OPEN_CHAPTER_LINK"
  }

}
