/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.deeplink

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.domain.chapter.interactor.FindOrInitChapterFromSource
import tachiyomi.domain.manga.interactor.FindOrInitMangaFromChapterKey
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.DeepLinkSource
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject
import tachiyomi.ui.deeplink.ChapterDeepLinkViewState as ViewState
import tachiyomi.ui.deeplink.ChapterDeepLinkAction as Action

class ChapterDeepLinkPresenter @Inject constructor(
  private val params: ChapterDeepLinkParams,
  private val sourceManager: SourceManager,
  private val findOrInitMangaFromChapterKey: FindOrInitMangaFromChapterKey,
  private val findOrInitChapterFromSource: FindOrInitChapterFromSource,
  private val schedulers: RxSchedulers
) : BasePresenter() {

  private val actions = PublishRelay.create<Action>()

  private val state = BehaviorRelay.create<ViewState>()

  val stateObserver: Observable<ViewState> = state

  init {
    actions
      .observeOn(schedulers.io)
      .reduxStore(
        initialState = ViewState(),
        sideEffects = listOf(::findChapterSideEffect),
        reducer = { state, action -> action.reduce(state) }
      )
      .distinctUntilChanged()
      .observeOn(schedulers.main)
      .subscribe(state::accept)
      .addTo(disposables)
  }

  @Suppress("unused_parameter")
  private fun findChapterSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    if (params.sourceId == null || params.chapterKey == null || params.chapterKey.isEmpty()) {
      return Observable.just(Action.Error(Exception(
        "Invalid input data: sourceId=${params.sourceId}, chapterKey=${params.chapterKey}"
      )))
    }

    val source = sourceManager.get(params.sourceId) as? DeepLinkSource
      ?: return Observable.just(Action.Error(Exception("Not a valid DeepLinkSource")))

    val findManga = findOrInitMangaFromChapterKey.interact(params.chapterKey, source)
      .subscribeOn(schedulers.io)
      .toObservable()
      .share()

    val findMangaIntent = findManga
      .map(Action::MangaReady)

    val findChapterIntent = findManga
      .flatMapSingle { findOrInitChapterFromSource.interact(params.chapterKey, it) }
      .map(Action::ChapterReady)

    return Observable.merge(findMangaIntent, findChapterIntent)
      .onErrorReturn(Action::Error)
  }

}
