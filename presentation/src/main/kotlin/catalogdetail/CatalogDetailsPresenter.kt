/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogdetail

import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tachiyomi.domain.catalog.interactor.GetInstalledCatalog
import tachiyomi.domain.catalog.interactor.UninstallCatalog
import tachiyomi.ui.presenter.BasePresenter
import tachiyomi.ui.presenter.EmptySideEffect
import javax.inject.Inject

class CatalogDetailsPresenter @Inject constructor(
  private val params: CatalogDetailsParams,
  private val getInstalledCatalog: GetInstalledCatalog,
  private val uninstallCatalog: UninstallCatalog
) : BasePresenter() {

  private val initialState = getInitialViewState()

  /**
   * Behavior subject containing the last emitted view state.
   */
  val state = ConflatedBroadcastChannel(initialState)

  private val store = scope.createStore(
    name = "Catalog details",
    initialState = initialState,
    sideEffects = getSideEffects(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.subscribeToChangedStateUpdatesInMain { state.offer(it) }
    loadCatalog()
  }

  private fun getInitialViewState(): ViewState {
    return ViewState()
  }

  private fun getSideEffects(): List<SideEffect<ViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<ViewState, Action>>()

    sideEffects += EmptySideEffect("Uninstall a catalog") f@{ stateFn, action ->
      if (action !is Action.UninstallCatalog) return@f null
      val catalog = stateFn().catalog ?: return@f null
      suspend { uninstallCatalog.await(catalog) }
    }

    return sideEffects
  }

  private fun loadCatalog() {
    scope.launch {
      getInstalledCatalog.subscribe(params.pkgName).collect {
        store.dispatch(Action.InstalledCatalog(it))
      }
    }
  }

  fun uninstallCatalog() {
    store.dispatch(Action.UninstallCatalog)
  }

}
