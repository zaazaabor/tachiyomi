package tachiyomi.ui.base

import androidx.annotation.CallSuper
import io.reactivex.Flowable
import io.reactivex.Observable
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

  fun <T> Observable<T>.logOnNext(): Observable<T> {
    return doOnNext { Timber.d(it.toString()) }
  }

}
