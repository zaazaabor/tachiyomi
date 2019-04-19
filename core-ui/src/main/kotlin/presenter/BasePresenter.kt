/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.presenter

import androidx.annotation.CallSuper
import com.freeletics.coredux.LogSink
import com.freeletics.coredux.StateReceiver
import com.freeletics.coredux.Store
import com.freeletics.coredux.log.common.LoggerLogSink
import com.freeletics.coredux.subscribeToChangedStateUpdates
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tachiyomi.core.di.AppScope
import tachiyomi.core.rx.CoroutineDispatchers
import tachiyomi.core.ui.BuildConfig
import timber.log.Timber
import timber.log.debug
import timber.log.info
import timber.log.warn

abstract class BasePresenter {

  protected val dispatchers = AppScope.getInstance<CoroutineDispatchers>()

  protected val job = SupervisorJob()

  protected val scope = CoroutineScope(job + dispatchers.computation)

  val disposables = CompositeDisposable()

  @CallSuper
  open fun destroy() {
    disposables.dispose()
    job.cancel()
  }

  fun <T> Observable<T>.logOnNext(): Observable<T> {
    return doOnNext { Timber.debug { it.toString() } }
  }

  fun <S : Any, A : Any> Store<S, A>.subscribeToChangedStateUpdatesInMain(
    stateReceiver: StateReceiver<S>
  ) {
    subscribeToChangedStateUpdates {
      scope.launch(dispatchers.main) { stateReceiver(it) }
    }
  }

  protected fun getLogSinks(): List<LogSink> {
    return if (BuildConfig.DEBUG) {
      listOf(TimberLogSink())
    } else {
      emptyList()
    }
  }

  private class TimberLogSink(scope: CoroutineScope = GlobalScope) : LoggerLogSink(scope) {
    override fun debug(tag: String, message: String, throwable: Throwable?) {
      Timber.debug(throwable) { message }
    }

    override fun info(tag: String, message: String, throwable: Throwable?) {
      Timber.info(throwable) { message }
    }

    override fun warning(tag: String, message: String, throwable: Throwable?) {
      Timber.warn(throwable) { message }
    }
  }

}
