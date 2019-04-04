/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.sync

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.domain.sync.interactor.Login
import tachiyomi.domain.sync.prefs.SyncPreferences
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject

class SyncPresenter @Inject constructor(
  private val login: Login,
  private val syncPreferences: SyncPreferences,
  schedulers: RxSchedulers
) : BasePresenter() {

  val state = BehaviorRelay.create<ViewState>()

  private val actions = PublishRelay.create<Action>()

  init {
    actions
      .observeOn(schedulers.io)
      .reduxStore(
        initialState = getInitialViewState(),
        sideEffects = listOf(
          ::loginSideEffect
        ),
        reducer = { state, action -> action.reduce(state) }
      )
      .distinctUntilChanged()
      .observeOn(schedulers.main)
      .subscribe(state::accept)
      .addTo(disposables)
  }

  private fun getInitialViewState(): ViewState {
    val isLogged = syncPreferences.token().isSet()
    return ViewState(
      isLogged = isLogged,
      isLoading = false
    )
  }

  private fun loginSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    val performLogin = actions.ofType<Action.Login>()
      .flatMap { action ->
        login.interact(action.address, action.username, action.password)
          .toObservable()
          .map { Action.Loading(false) }
          .startWith(Action.Loading(true))
      }

    val listenLoggedIn = syncPreferences.token().asObservable()
      .skip(1)
      .map { Action.Logged(it.isNotEmpty()) }

    return Observable.merge(performLogin, listenLoggedIn)
  }

  fun login(address: String, username: String, password: String) {
    actions.accept(Action.Login(address, username, password))
  }

}
