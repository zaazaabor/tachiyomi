package tachiyomi.ui.base

import android.support.annotation.CallSuper
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

abstract class BasePresenter {

  val disposables = CompositeDisposable()

  @CallSuper
  open fun destroy() {
    disposables.dispose()
  }

  fun <T> Flowable<T>.logOnNext(): Flowable<T> {
    return doOnNext { Timber.d(it.toString()) }
  }

}
