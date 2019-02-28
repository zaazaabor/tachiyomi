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
import tachiyomi.domain.category.Category
import tachiyomi.domain.category.interactor.CreateCategoryWithName
import tachiyomi.domain.category.interactor.DeleteCategories
import tachiyomi.domain.category.interactor.RenameCategory
import tachiyomi.domain.category.interactor.ReorderCategory
import tachiyomi.domain.category.interactor.SubscribeCategories
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject

class CategoryPresenter @Inject constructor(
  private val subscribeCategories: SubscribeCategories,
  private val createCategoryWithName: CreateCategoryWithName,
  private val deleteCategories: DeleteCategories,
  private val renameCategory: RenameCategory,
  private val reorderCategory: ReorderCategory,
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
          ::createCategorySideEffect,
          ::deleteCategorySideEffect,
          ::renameCategorySideEffect,
          ::reorderCategorySideEffect
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

  @Suppress("unused_parameter")
  private fun deleteCategorySideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    return actions.ofType<Action.DeleteCategories>()
      .flatMap {
        deleteCategories.interact(it.categoryIds)
          .andThen(Observable.just(Action.UnselectCategories))
      }
  }

  @Suppress("unused_parameter")
  private fun renameCategorySideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    return actions.ofType<Action.RenameCategory>()
      .flatMap {
        renameCategory.interact(it.categoryId, it.newName)
          .andThen(Observable.just(Action.UnselectCategories))
      }
  }

  @Suppress("unused_parameter")
  private fun reorderCategorySideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    return actions.ofType<Action.ReorderCategory>()
      .flatMap {
        reorderCategory.interact(it.category, it.newPosition).toObservable<Action>()
      }
  }

  fun createCategory(name: String) {
    actions.accept(Action.CreateCategory(name))
  }

  fun deleteCategories(categories: Set<Long>) {
    actions.accept(Action.DeleteCategories(categories))
  }

  fun renameCategory(categoryId: Long, newName: String) {
    actions.accept(Action.RenameCategory(categoryId, newName))
  }

  fun reorderCategory(category: Category, newPosition: Int) {
    actions.accept(Action.ReorderCategory(category, newPosition))
  }

  fun toggleCategorySelection(category: Category) {
    actions.accept(Action.ToggleCategorySelection(category))
  }

  fun unselectCategories() {
    actions.accept(Action.UnselectCategories)
  }

  fun getCategory(id: Long): Category? {
    return state.value?.categories?.find { it.id == id }
  }

}
