package tachiyomi.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.linearLayout
import tachiyomi.ui.category.CategoryController

class MainActivity : AppCompatActivity() {

  private lateinit var router: Router

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    linearLayout {
      val container = ankoView(::ChangeHandlerFrameLayout, 0) {

      }
      router = Conductor.attachRouter(this@MainActivity, container, savedInstanceState)
      if (!router.hasRootController()) {
        router.setRoot(RouterTransaction.with(CategoryController()))
      }
    }
  }

  override fun onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed()
    }
  }
}
