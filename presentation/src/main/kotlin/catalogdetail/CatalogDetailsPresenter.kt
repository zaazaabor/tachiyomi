/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogdetail

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.core.rx.filterNotNull
import tachiyomi.domain.catalog.interactor.SubscribeInstalledCatalog
import tachiyomi.domain.catalog.interactor.UninstallCatalog
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject

class CatalogDetailsPresenter @Inject constructor(
  private val params: CatalogDetailsParams,
  private val schedulers: RxSchedulers,
  private val subscribeInstalledCatalog: SubscribeInstalledCatalog,
  private val uninstallCatalog: UninstallCatalog
) : BasePresenter() {

  /**
   * Subject which allows emitting actions and subscribing to a specific one while supporting
   * backpressure.
   */
  private val actions = PublishRelay.create<Action>()

  /**
   * Behavior subject containing the last emitted view state.
   */
  private val state = BehaviorRelay.create<ViewState>()

  /**
   * State subject as a consumer-only observable.
   */
  val stateObserver: Observable<ViewState> = state

  init {
    actions
      .observeOn(schedulers.io)
      .reduxStore(
        initialState = getInitialViewState(),
        sideEffects = listOf(
          ::loadInstalledCatalogSideEffect,
          ::uninstallCatalogSideEffect
        ),
        reducer = { state, action -> action.reduce(state) }
      )
      .distinctUntilChanged()
      .observeOn(schedulers.main)
      .subscribe(state::accept)
      .addTo(disposables)
  }

  private fun getInitialViewState(): ViewState {
    return ViewState()
  }

  @Suppress("unused_parameter")
  private fun loadInstalledCatalogSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    return subscribeInstalledCatalog.interact(params.pkgName)
      .map<Action> { Action.InstalledCatalog(it.get()) }
  }

  private fun uninstallCatalogSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    return actions.ofType<Action.UninstallCatalog>()
      .filterNotNull { stateFn().catalog }
      .flatMap { catalog ->
        uninstallCatalog.interact(catalog)
          .onErrorComplete()
          .toObservable<Action>()
      }
  }

  fun uninstallCatalog() {
    actions.accept(Action.UninstallCatalog)
  }

}
