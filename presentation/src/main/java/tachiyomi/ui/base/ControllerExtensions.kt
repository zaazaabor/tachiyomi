package tachiyomi.ui.base

import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler

fun Controller.withHorizontalTransaction(): RouterTransaction {
  return RouterTransaction.with(this)
    .pushChangeHandler(HorizontalChangeHandler())
    .popChangeHandler(HorizontalChangeHandler())
}

fun Controller.withFadeTransaction(): RouterTransaction {
  return RouterTransaction.with(this)
    .pushChangeHandler(FadeChangeHandler())
    .popChangeHandler(FadeChangeHandler())
}
