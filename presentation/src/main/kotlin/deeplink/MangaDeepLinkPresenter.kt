/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.deeplink

import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import kotlinx.coroutines.launch
import tachiyomi.domain.manga.interactor.GetOrAddMangaFromSource
import tachiyomi.source.model.MangaInfo
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject
import tachiyomi.ui.deeplink.MangaDeepLinkAction as Action
import tachiyomi.ui.deeplink.MangaDeepLinkViewState as ViewState

class MangaDeepLinkPresenter @Inject constructor(
  private val params: MangaDeepLinkParams,
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource
) : BasePresenter() {

  private val state = BehaviorRelay.create<ViewState>()

  val stateObserver: Observable<ViewState> = state

  private val store = scope.createStore(
    name = "Manga deeplink presenter",
    initialState = getInitialViewState(),
    sideEffects = getSideEffects(),
    logSinks = getLogSinks(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.subscribeToChangedStateUpdatesInMain { state.accept(it) }
    loadManga()
  }

  private fun getInitialViewState(): ViewState {
    return ViewState()
  }

  private fun getSideEffects(): List<SideEffect<ViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<ViewState, Action>>()
    return sideEffects
  }

  private fun loadManga() {
    if (params.sourceId == null || params.mangaKey == null || params.mangaKey.isEmpty()) {
      store.dispatch(Action.Error(
        Exception("Invalid input data: sourceId=${params.sourceId}, mangaKey=${params.mangaKey}"))
      )
      return
    }

    scope.launch {
      val mangaInfo = MangaInfo(key = params.mangaKey, title = "")

      try {
        val manga = getOrAddMangaFromSource.await(mangaInfo, params.sourceId)
        store.dispatch(Action.MangaReady(manga.id))
      } catch (e: Exception) {
        store.dispatch(Action.Error(e))
      }
    }
  }

}
