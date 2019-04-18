/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.sync

import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import com.jakewharton.rxrelay2.BehaviorRelay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import tachiyomi.core.rx.asFlow
import tachiyomi.domain.sync.interactor.Login
import tachiyomi.domain.sync.prefs.SyncPreferences
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject

class SyncPresenter @Inject constructor(
  private val login: Login,
  private val syncPreferences: SyncPreferences
) : BasePresenter() {

  val state = BehaviorRelay.create<ViewState>()

  private val store = scope.createStore(
    name = "Sync presenter",
    initialState = getInitialViewState(),
    sideEffects = getSideEffects(),
    logSinks = getLogSinks(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.subscribeToChangedStateUpdatesInMain { state.accept(it) }
    listenLoggedin()
  }

  private fun getInitialViewState(): ViewState {
    val isLogged = syncPreferences.token().isSet()
    return ViewState(
      isLogged = isLogged,
      isLoading = false
    )
  }

  private fun getSideEffects(): List<SideEffect<ViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<ViewState, Action>>()

    sideEffects += FlowSideEffect("Login") f@{ stateFn, action, _, handler ->
      if (action !is Action.Login) return@f null

      handler {
        flow {
          emit(Action.Loading(true))
          val result = login.interact(action.address, action.username, action.password).await()
          emit(Action.Loading(false))
        }
      }
    }

    return sideEffects
  }

  private fun listenLoggedin() {
    scope.launch {
      syncPreferences.token().asObservable().skip(1).asFlow().collect {
        store.dispatch(Action.Logged(it.isNotEmpty()))
      }
    }
  }

  fun login(address: String, username: String, password: String) {
    store.dispatch(Action.Login(address, username, password))
  }

}
