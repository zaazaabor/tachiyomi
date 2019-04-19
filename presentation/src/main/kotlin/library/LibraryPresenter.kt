/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import com.jakewharton.rxrelay2.BehaviorRelay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tachiyomi.domain.library.interactor.GetLibraryCategory
import tachiyomi.domain.library.interactor.GetUserCategories
import tachiyomi.domain.library.interactor.SetCategoriesForMangas
import tachiyomi.domain.library.interactor.UpdateLibraryCategory
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.prefs.LibraryPreferences
import tachiyomi.ui.presenter.BasePresenter
import tachiyomi.ui.presenter.FlowSwitchSideEffect
import javax.inject.Inject

class LibraryPresenter @Inject constructor(
  private val getUserCategories: GetUserCategories,
  private val getLibraryCategory: GetLibraryCategory,
  private val setCategoriesForMangas: SetCategoriesForMangas,
  private val libraryPreferences: LibraryPreferences,
  private val updateLibraryCategory: UpdateLibraryCategory
) : BasePresenter() {

  val state = BehaviorRelay.create<ViewState>()

  private val lastSortPreference = libraryPreferences.lastSorting()

  private val filtersPreference = libraryPreferences.filters()

  private val lastUsedCategoryPreference = libraryPreferences.lastUsedCategory()

  private val store = scope.createStore(
    name = "Library presenter",
    initialState = getInitialViewState(),
    sideEffects = getSideEffects(),
    logSinks = getLogSinks(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.subscribeToChangedStateUpdatesInMain { state.accept(it) }
    store.dispatch(Action.Init)
  }

  private fun getInitialViewState(): ViewState {
    val lastSort = lastSortPreference.get()
    return ViewState(
      categories = emptyList(),
      library = emptyList(),
      filters = emptyList(),
      sort = lastSort
    )
  }

  private fun getSideEffects(): List<SideEffect<ViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<ViewState, Action>>()

    sideEffects += FlowSwitchSideEffect("Subscribe user categories") f@{ _, action ->
      if (action !is Action.Init) return@f null

      suspend {
        var initialSet = false
        getUserCategories.subscribe(true).flatMapConcat { categories ->
          if (initialSet) {
            flowOf(Action.CategoriesUpdate(categories))
          } else {
            initialSet = true

            val lastCategoryId = lastUsedCategoryPreference.get()
            val category = categories.find { it.id == lastCategoryId }
              ?: categories.firstOrNull()

            flowOf(Action.CategoriesUpdate(categories), Action.SetSelectedCategory(category))
          }
        }
      }
    }

    var subscribedCategory: Long? = null
    sideEffects += FlowSwitchSideEffect("Subscribe selected category") f@{ stateFn, action ->
      if (action !is Action.SetSelectedCategory) return@f null
      val selectedId = stateFn().selectedCategoryId
      if (subscribedCategory == selectedId) return@f null
      subscribedCategory = selectedId

      suspend {
        if (selectedId == null) {
          flowOf(Action.LibraryUpdate(emptyList()))
        } else {
          lastUsedCategoryPreference.set(selectedId)

          getLibraryCategory.subscribe(selectedId)
            .map { Action.LibraryUpdate(it) }
        }
      }
    }

    sideEffects += FlowSwitchSideEffect("Update selected category") f@{ stateFn, action ->
      if (action !is Action.UpdateCategory) return@f null
      val categoryId = stateFn().selectedCategoryId ?: return@f null

      suspend {
        GlobalScope.launch {
          updateLibraryCategory.execute(categoryId).awaitWork()
        }

        flow {
          emit(Action.ShowUpdatingCategory(true))
          delay(1000)
          emit(Action.ShowUpdatingCategory(false))
        }
      }
    }

    return sideEffects
  }

//  @Suppress("unused_parameter")
//  private fun librarySideEffect(
//    actions: Observable<Action>,
//    stateFn: StateAccessor<ViewState>
//  ): Observable<Action.LibraryUpdate> {
//    val state = stateFn()
//    //getLibrary.setFilters(state.filters)
//    getLibrary.setSorting(state.sort)
//
//    return actions.ofType<Action.SetFilters>()
//      .startWith(Action.SetFilters(state.filters)) // TODO check threading
//      .switchMap { getLibrary.interact(it.filters).onBackpressureLatest().toObservable() }
//      .subscribeOn(schedulers.io)
//      .map(Action::LibraryUpdate)
//  }

//  @Suppress("unused_parameter")
//  private fun setFiltersSideEffect(
//    actions: Observable<Action>,
//    stateFn: StateAccessor<ViewState>
//  ): Observable<Action> {
//    return actions.ofType<Action.SetFilters>()
//      .flatMap { action ->
//        filtersPreference.set(action.filters)
//        //getLibrary.setFilters(action.filters)
//        Observable.empty<Action>()
//      }
//  }
//
//  @Suppress("unused_parameter")
//  private fun setSortSideEffect(
//    actions: Observable<Action>,
//    stateFn: StateAccessor<ViewState>
//  ): Observable<Action> {
//    return actions.ofType<Action.SetSorting>()
//      .flatMap { action ->
//        lastSortPreference.set(action.sort)
//        //getLibrary.setSorting(action.sort)
//        Observable.empty<Action>()
//      }
//  }

  fun setSelectedCategory(position: Int) {
    val category = state.value?.categories?.getOrNull(position) ?: return
    store.dispatch(Action.SetSelectedCategory(category))
  }

  fun updateSelectedCategory() {
    store.dispatch(Action.UpdateCategory)
  }

  fun toggleMangaSelection(manga: LibraryManga) {
    store.dispatch(Action.ToggleSelection(manga))
  }

  fun unselectMangas() {
    store.dispatch(Action.UnselectMangas)
  }

  fun setCategoriesForMangas(categoryIds: Collection<Long>, mangaIds: Collection<Long>) {
    scope.launch {
      val result = setCategoriesForMangas.await(categoryIds, mangaIds)
      if (result is SetCategoriesForMangas.Result.Success) {
        unselectMangas()
      }
    }
  }

  fun getCategories(): List<Category> {
    return state.value?.categories.orEmpty()
  }

}
