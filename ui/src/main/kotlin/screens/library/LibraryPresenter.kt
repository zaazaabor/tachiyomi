/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.library

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.data.library.prefs.LibraryPreferences
import tachiyomi.domain.library.interactor.GetLibrary
import tachiyomi.domain.library.model.LibraryCategory
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject

class LibraryPresenter @Inject constructor(
  private val getLibrary: GetLibrary,
  private val libraryPreferences: LibraryPreferences,
  private val schedulers: RxSchedulers
) : BasePresenter() {

  val state = BehaviorRelay.create<LibraryViewState>()

  private val actions = PublishRelay.create<Action>()

  private val lastSortPreference = libraryPreferences.lastSorting()

  private val filtersPreference = libraryPreferences.filters()

  init {
    actions
      .observeOn(schedulers.io)
      .reduxStore(
        initialState = getInitialViewState(),
        sideEffects = listOf(::librarySideEffect, ::setFiltersSideEffect, ::setSortSideEffect),
        reducer = { state, action -> action.reduce(state) }
      )
      .distinctUntilChanged()
      .logOnNext()
      .observeOn(schedulers.main)
      .subscribe(state::accept)
      .addTo(disposables)
  }

  private fun getInitialViewState(): LibraryViewState {
    val lastSort = lastSortPreference.get()
    return LibraryViewState(
      emptyList(),
      filters = emptyList(),
      sort = lastSort
    )
  }

  @Suppress("unused_parameter")
  private fun librarySideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<LibraryViewState>
  ): Observable<Action.LibraryUpdate> {
    val state = stateFn()
    getLibrary.setFilters(state.filters)
    getLibrary.setSorting(state.sort)

    return getLibrary.interact()
      .subscribeOn(schedulers.io)
      .toObservable()
      .map(Action::LibraryUpdate)
  }

  @Suppress("unused_parameter")
  private fun setFiltersSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<LibraryViewState>
  ): Observable<Action> {
    return actions.ofType(Action.SetFilters::class.java)
      .flatMap { action ->
        filtersPreference.set(action.filters)
        getLibrary.setFilters(action.filters)
        Observable.empty<Action>()
      }
  }

  @Suppress("unused_parameter")
  private fun setSortSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<LibraryViewState>
  ): Observable<Action> {
    return actions.ofType(Action.SetSorting::class.java)
      .flatMap { action ->
        lastSortPreference.set(action.sort)
        getLibrary.setSorting(action.sort)
        Observable.empty<Action>()
      }
  }

}

private sealed class Action {

  data class SetFilters(val filters: List<LibraryFilter>) : Action() {
    override fun reduce(state: LibraryViewState) =
      state.copy(filters = filters)
  }

  data class SetSorting(val sort: LibrarySort) : Action() {
    override fun reduce(state: LibraryViewState) =
      state.copy(sort = sort)
  }

  data class LibraryUpdate(val library: List<LibraryCategory>) : Action() {
    override fun reduce(state: LibraryViewState) =
      state.copy(library = library)
  }

  open fun reduce(state: LibraryViewState) = state
}
