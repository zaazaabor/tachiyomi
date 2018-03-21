package tachiyomi.ui.base

import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*

abstract class BaseController(
  bundle: Bundle? = null
) : RestoreViewOnCreateController(bundle), LayoutContainer {

  private var viewDisposables = CompositeDisposable()

  override val containerView: View?
    get() = view

  init {
    addLifecycleListener(object : LifecycleListener() {
      override fun postCreateView(controller: Controller, view: View) {
        onViewCreated(view)
      }
    })
  }

  @CallSuper
  open fun onViewCreated(view: View) {
    if (viewDisposables.isDisposed) {
      viewDisposables = CompositeDisposable()
    }
  }

  @CallSuper
  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    viewDisposables.dispose()
    clearFindViewByIdCache()
  }

  fun <T> Observable<T>.subscribeWithView(onNext: (T) -> Unit): Disposable {
    return subscribe(onNext).also { viewDisposables.add(it) }
  }

  fun <T> Flowable<T>.subscribeWithView(onNext: (T) -> Unit): Disposable {
    return subscribe(onNext).also { viewDisposables.add(it) }
  }

}
