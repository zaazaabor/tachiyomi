package tachiyomi.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import kotlinx.android.synthetic.main.main_activity.*
import tachiyomi.app.R
import tachiyomi.ui.base.withoutTransition
import tachiyomi.ui.home.HomeController

class MainActivity : AppCompatActivity() {

  private var router: Router? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Do not let the launcher create a new activity http://stackoverflow.com/questions/16283079
    if (!isTaskRoot) {
      finish()
      return
    }

    // Init view
    setContentView(R.layout.main_activity)

    // Init conductor
    val router = Conductor.attachRouter(this, main_controller_container, savedInstanceState)
    this.router = router

    if (!router.hasRootController()) {
      // Set start screen
      router.setRoot(HomeController().withoutTransition())
    }
  }

  override fun onNewIntent(intent: Intent) {
    if (!handleIntentAction(intent)) {
      super.onNewIntent(intent)
    }
  }

  private fun handleIntentAction(intent: Intent): Boolean {
    return false
  }

  override fun onBackPressed() {
    val router = router
    if (router == null || !router.handleBack()) {
      super.onBackPressed()
    }
  }

  companion object {
    val SHORTCUT_DEEPLINK_MANGA = "TODO"
    val SHORTCUT_DEEPLINK_CHAPTER = "TODO"
  }

}
