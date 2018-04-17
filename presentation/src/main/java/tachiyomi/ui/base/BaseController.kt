package tachiyomi.ui.base

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
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

  private var _title: String? = null

  init {
    addLifecycleListener(object : LifecycleListener() {
      override fun postCreateView(controller: Controller, view: View) {
        onViewCreated(view)
      }

      override fun onChangeStart(
        controller: Controller,
        changeHandler: ControllerChangeHandler,
        changeType: ControllerChangeType
      ) {
        if (changeType.isEnter) {
          setToolbarTitle()
        }
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

  open fun getTitle(): String? {
    return _title
  }

  fun requestTitle(title: String) {
    if (_title == title) return
    _title = title

    val lastTransaction = router.backstack.lastOrNull { it.controller() is BaseController }
    if (this == lastTransaction?.controller()) {
      setToolbarTitle()
    }
  }

  private fun setToolbarTitle() {
    val title = getTitle()
    if (title != null) {
      (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }
  }

  fun <T> Observable<T>.subscribeWithView(onNext: (T) -> Unit): Disposable {
    return subscribe(onNext).also { viewDisposables.add(it) }
  }

  fun <T> Flowable<T>.subscribeWithView(onNext: (T) -> Unit): Disposable {
    return subscribe(onNext).also { viewDisposables.add(it) }
  }

}
