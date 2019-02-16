/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.category

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.domain.category.interactor.CreateCategoryWithName
import tachiyomi.domain.category.interactor.SubscribeCategories
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject

class CategoryPresenter @Inject constructor(
  private val subscribeCategories: SubscribeCategories,
  private val createCategoryWithName: CreateCategoryWithName,
  private val schedulers: RxSchedulers
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
          ::loadCategoriesSideEffect,
          ::createCategorySideEffect
        ),
        reducer = { state, action -> action.reduce(state) }
      )
      .distinctUntilChanged()
      .logOnNext()
      .observeOn(schedulers.main)
      .subscribe(state::accept)
      .addTo(disposables)
  }

  private fun getInitialViewState(): ViewState {
    return ViewState()
  }

  @Suppress("unused_parameter")
  private fun loadCategoriesSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    return subscribeCategories.interact()
      .onBackpressureLatest()
      .toObservable()
      .map(Action::CategoriesUpdate)
  }

  @Suppress("unused_parameter")
  private fun createCategorySideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    return actions.ofType<Action.CreateCategory>()
      .flatMapMaybe {
        createCategoryWithName.interact(it.name)
          .toMaybe<Action>()
          .onErrorReturn(Action::Error)
      }
  }

  fun createCategory(name: String) {
    actions.accept(Action.CreateCategory(name))
  }

}
