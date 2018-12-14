/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.ui.base.BasePresenter
import javax.inject.Inject

class CatalogsPresenter @Inject constructor(
  private val catalogRepository: CatalogRepository,
  private val schedulers: RxSchedulers
) : BasePresenter() {

  private val actions = PublishRelay.create<Action>()

  val state = BehaviorRelay.create<CatalogsViewState>()

  init {
    actions
      .observeOn(schedulers.io)
      .reduxStore(
        initialState = CatalogsViewState(),
        sideEffects = listOf(::loadCatalogsSideEffect),
        reducer = ::reduce
      )
      .distinctUntilChanged()
      .observeOn(schedulers.main)
      .subscribe(state::accept)
      .addTo(disposables)
  }

  @Suppress("unused_parameter")
  private fun loadCatalogsSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<CatalogsViewState>
  ): Observable<Action> {
    val internalCatalogs = catalogRepository.getInternalCatalogsFlowable()
      .subscribeOn(schedulers.io)
      .map(Action::InternalCatalogUpdate)
      .toObservable()

    val installedCatalogs = catalogRepository.getInstalledCatalogsFlowable()
      .subscribeOn(schedulers.io)
      .map(Action::InstalledCatalogUpdate)
      .toObservable()

    return Observable.merge(internalCatalogs, installedCatalogs)
  }

}

private sealed class Action {
  data class InternalCatalogUpdate(val catalogs: List<Catalog.Internal>) : Action()
  data class InstalledCatalogUpdate(val catalogs: List<Catalog.Installed>) : Action()
}

private fun reduce(state: CatalogsViewState, action: Action): CatalogsViewState {
  return when (action) {
    is Action.InternalCatalogUpdate -> state.copy(internal = action.catalogs)
    is Action.InstalledCatalogUpdate -> state.copy(installed = action.catalogs)
  }
}
