package tachiyomi.ui.base

import android.os.Bundle
import tachiyomi.core.di.AppScope
import toothpick.Toothpick
import toothpick.config.Module

abstract class MvpController<P : BasePresenter>(
  bundle: Bundle? = null
) : BaseController(bundle) {

  @Suppress("LeakingThis")
  private val scope = AppScope.subscope(this).also { scope ->
    getModule()?.let { scope.installModules(it) }
  }

  val presenter: P by lazy { scope.getInstance(getPresenterClass()) }

  abstract fun getPresenterClass(): Class<P>

  open fun getModule(): Module? {
    return null
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.destroy()
    Toothpick.closeScope(this)
  }

}
