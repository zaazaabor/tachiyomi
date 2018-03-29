package tachiyomi.ui.base

import android.os.Bundle
import tachiyomi.openApplicationScope
import toothpick.Toothpick
import toothpick.config.Module

abstract class MvpScopedController<P : BasePresenter>(
  bundle: Bundle? = null
) : MvpController<P>(bundle) {

  @Suppress("LeakingThis")
  private val scope = openApplicationScope(this).also { scope ->
    getModule()?.let { scope.installModules(it) }
  }

  abstract fun getPresenterClass(): Class<P>

  open fun getModule(): Module? {
    return null
  }

  override fun createPresenter(): P {
    return scope.getInstance(getPresenterClass())
  }

  override fun onDestroy() {
    super.onDestroy()
    Toothpick.closeScope(scope)
  }

}
