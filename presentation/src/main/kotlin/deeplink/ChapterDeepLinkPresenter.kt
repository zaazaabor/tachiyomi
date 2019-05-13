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
import tachiyomi.domain.manga.interactor.FindOrInitChapterFromSource
import tachiyomi.domain.manga.interactor.FindOrInitMangaFromChapterKey
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.DeepLinkSource
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject
import tachiyomi.ui.deeplink.ChapterDeepLinkAction as Action
import tachiyomi.ui.deeplink.ChapterDeepLinkViewState as ViewState

class ChapterDeepLinkPresenter @Inject constructor(
  private val params: ChapterDeepLinkParams,
  private val sourceManager: SourceManager,
  private val findOrInitMangaFromChapterKey: FindOrInitMangaFromChapterKey,
  private val findOrInitChapterFromSource: FindOrInitChapterFromSource
) : BasePresenter() {

  private val state = BehaviorRelay.create<ViewState>()

  val stateObserver: Observable<ViewState> = state

  private val store = scope.createStore(
    name = "Chapter deeplink presenter",
    initialState = getInitialViewState(),
    sideEffects = getSideEffects(),
    logSinks = getLogSinks(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.subscribeToChangedStateUpdatesInMain { state.accept(it) }
    loadChapter()
  }

  private fun getInitialViewState(): ViewState {
    return ViewState()
  }

  private fun getSideEffects(): List<SideEffect<ViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<ViewState, Action>>()
    return sideEffects
  }

  private fun loadChapter() {
    scope.launch {
      if (params.sourceId == null || params.chapterKey == null || params.chapterKey.isEmpty()) {
        store.dispatch(Action.Error(Exception(
          "Invalid input data: sourceId=${params.sourceId}, chapterKey=${params.chapterKey}"
        )))
        return@launch
      }

      val source = sourceManager.get(params.sourceId) as? DeepLinkSource
      if (source == null) {
        store.dispatch(Action.Error(Exception("Not a valid DeepLinkSource")))
        return@launch
      }

      try {
        val manga = findOrInitMangaFromChapterKey.await(params.chapterKey, source)
        store.dispatch(Action.MangaReady(manga))

        val chapter = findOrInitChapterFromSource.await(params.chapterKey, manga)
        store.dispatch(Action.ChapterReady(chapter!!))
      } catch (e: Exception) {
        store.dispatch(Action.Error(e))
      }
    }
  }

}
