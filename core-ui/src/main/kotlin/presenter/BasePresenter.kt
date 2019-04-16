/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.presenter

import androidx.annotation.CallSuper
import com.freeletics.coredux.LogEvent
import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.SideEffectLogger
import com.freeletics.coredux.StateAccessor
import com.freeletics.coredux.StateReceiver
import com.freeletics.coredux.Store
import com.freeletics.coredux.log.common.LoggerLogSink
import com.freeletics.coredux.subscribeToChangedStateUpdates
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tachiyomi.core.di.AppScope
import tachiyomi.core.rx.CoroutineDispatchers
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

  class FlowSideEffect<S : Any, A : Any>(
    override val name: String,
    private val sideEffect: (
      state: StateAccessor<S>,
      action: A,
      logger: SideEffectLogger,
      handler: (suspend (name: String) -> Flow<A>?) -> Job
    ) -> Job?
  ) : SideEffect<S, A> {

    override fun CoroutineScope.start(
      input: ReceiveChannel<A>,
      stateAccessor: StateAccessor<S>,
      output: SendChannel<A>,
      logger: SideEffectLogger
    ) = launch(context = CoroutineName(name)) {
      var job: Job? = null
      for (action in input) {
        //logger.logSideEffectEvent { LogEvent.SideEffectEvent.InputAction(name, action) }
        sideEffect(stateAccessor, action, logger) { handler ->
          job?.run {
            if (isActive) {
              logger.logSideEffectEvent {
                LogEvent.SideEffectEvent.Custom(
                  name,
                  "Cancelling previous job on new $action action"
                )
              }
            }
            cancel()
          }
          launch {
            handler(name)?.let { flow ->
              flow.collect {
                logger.logSideEffectEvent {
                  LogEvent.SideEffectEvent.DispatchingToReducer(name, it)
                }
                output.send(it)
              }
            }
          }
        }?.let { job = it }
      }
    }
  }

  class MultiFlowSideEffect<S : Any, A : Any>(
    override val name: String,
    private val sideEffect: (
      state: StateAccessor<S>,
      action: A,
      logger: SideEffectLogger,
      handler: (suspend (name: String) -> Flow<A>?) -> Job
    ) -> Job?
  ) : SideEffect<S, A> {

    override fun CoroutineScope.start(
      input: ReceiveChannel<A>,
      stateAccessor: StateAccessor<S>,
      output: SendChannel<A>,
      logger: SideEffectLogger
    ) = launch(context = CoroutineName(name)) {
      for (action in input) {
        //logger.logSideEffectEvent { LogEvent.SideEffectEvent.InputAction(name, action) }
        sideEffect(stateAccessor, action, logger) { handler ->
          launch {
            handler(name)?.let { flow ->
              flow.collect {
                logger.logSideEffectEvent {
                  LogEvent.SideEffectEvent.DispatchingToReducer(name, it)
                }
                output.send(it)
              }
            }
          }
        }
      }
    }
  }

  class TimberLogSink(scope: CoroutineScope = GlobalScope) : LoggerLogSink(scope) {
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
