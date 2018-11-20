/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import com.bluelinelabs.conductor.Router
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*
import tachiyomi.app.BuildConfig
import timber.log.Timber

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

      override fun onChangeStart(
        controller: Controller,
        changeHandler: ControllerChangeHandler,
        changeType: ControllerChangeType
      ) {
        if (BuildConfig.DEBUG && changeType.isEnter) {
          printBackstack()
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

  fun <T> Observable<T>.subscribeWithView(onNext: (T) -> Unit): Disposable {
    return subscribe(onNext).also { viewDisposables.add(it) }
  }

  fun <T> Flowable<T>.subscribeWithView(onNext: (T) -> Unit): Disposable {
    return subscribe(onNext).also { viewDisposables.add(it) }
  }

  fun findRootRouter(): Router {
    var currentRouter = router
    var parent = parentController
    while (parent != null) {
      currentRouter = parent.router
      parent = parent.parentController
    }
    return currentRouter
  }

  fun printBackstack() {
    val rootRouter = findRootRouter()
    val builder = StringBuilder()
    builder.appendln("Current backstack:")
    printBackstack(builder, rootRouter, 0)
    Timber.w(builder.toString())
  }

  private fun printBackstack(builder: StringBuilder, router: Router, level: Int) {
    router.backstack.forEach { transaction ->
      val controller = transaction.controller()
      for (i in 0 until level) {
        builder.append("\t")
      }
      builder.appendln(controller.toString())
      for (childRouter in controller.childRouters) {
        printBackstack(builder, childRouter, level + 1)
      }
    }
  }

}
