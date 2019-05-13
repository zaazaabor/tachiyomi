/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.manga

import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.interactor.SyncChaptersFromSource
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.presenter.BasePresenter
import tachiyomi.ui.presenter.EmptySideEffect
import javax.inject.Inject

class MangaPresenter @Inject constructor(
  private val mangaId: Long?,
  private val getManga: GetManga,
  private val mangaInitializer: MangaInitializer,
  private val syncChaptersFromSource: SyncChaptersFromSource
) : BasePresenter() {

  private val stateRelay = BehaviorRelay.create<MangaViewState>()

  val stateObserver: Observable<MangaViewState> = stateRelay

  private val store = scope.createStore(
    name = "Manga presenter",
    initialState = getInitialViewState(),
    sideEffects = getSideEffects(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.subscribeToChangedStateUpdatesInMain { stateRelay.accept(it) }
    loadManga()
  }

  private fun getInitialViewState(): MangaViewState {
    return MangaViewState()
  }

  private fun getSideEffects(): List<SideEffect<MangaViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<MangaViewState, Action>>()

    sideEffects += EmptySideEffect("Update details") f@{ stateFn, action ->
      if (action !is Action.UpdateDetails) return@f null
      val manga = stateFn().header?.manga ?: return@f null
      suspend { mangaInitializer.await(manga) }
    }

    return sideEffects
  }

  private fun loadManga() {
    scope.launch {
      var initialized = false
      getManga.subscribe(mangaId!!).collect { manga ->
        if (manga != null) {
          store.dispatch(Action.MangaUpdate(manga))
          if (!initialized) {
            initialized = true
            store.dispatch(Action.UpdateDetails)
          }
        }
      }
    }
  }

}

private sealed class Action {
  data class MangaUpdate(val manga: Manga) : Action() {
    override fun reduce(state: MangaViewState) = state.copy(
      header = state.header?.copy(manga = manga) ?: MangaHeader(manga = manga)
    )
  }

  object UpdateDetails : Action()

  open fun reduce(state: MangaViewState): MangaViewState = state
}
