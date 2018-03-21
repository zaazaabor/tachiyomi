package tachiyomi.ui.base

import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter {

  val disposables = CompositeDisposable()

  @CallSuper
  open fun destroy() {
    disposables.dispose()
  }
}
