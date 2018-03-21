package tachiyomi.ui.base

import android.os.Bundle

abstract class MvpController<out P : BasePresenter>(
  bundle: Bundle? = null
) : BaseController(bundle) {

  val presenter: P by lazy { createPresenter() }

  override fun onDestroy() {
    presenter.destroy()
    super.onDestroy()
  }

  abstract fun createPresenter(): P
}
