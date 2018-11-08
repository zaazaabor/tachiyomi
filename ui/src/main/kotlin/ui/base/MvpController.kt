package tachiyomi.ui.base

import android.os.Bundle

abstract class MvpController<out P : BasePresenter>(
  bundle: Bundle? = null
) : BaseController(bundle) {

  val presenter: P by lazy { createPresenter() }

  abstract fun createPresenter(): P

  override fun onDestroy() {
    super.onDestroy()
    presenter.destroy()
  }

}
