/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.deeplink

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.domain.manga.interactor.GetOrAddMangaFromSource
import tachiyomi.source.model.MangaInfo
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject
import tachiyomi.ui.screens.deeplink.MangaDeepLinkViewState as ViewState
import tachiyomi.ui.screens.deeplink.MangaDeepLinkAction as Action

class MangaDeepLinkPresenter @Inject constructor(
  private val params: MangaDeepLinkParams,
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource,
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
        sideEffects = listOf(::findMangaSideEffect),
        reducer = { state, action -> action.reduce(state) }
      )
      .distinctUntilChanged()
      .observeOn(schedulers.main)
      .subscribe(state::accept)
      .addTo(disposables)
  }

  @Suppress("unused_parameter")
  private fun findMangaSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    if (params.sourceId == null || params.mangaKey == null || params.mangaKey.isEmpty()) {
      return Observable.just(Action.Error(
        Exception("Invalid input data: sourceId=${params.sourceId}, mangaKey=${params.mangaKey}"))
      )
    }

    val mangaInfo = MangaInfo(key = params.mangaKey, title = "")

    return getOrAddMangaFromSource.interact(mangaInfo, params.sourceId)
      .toObservable()
      .subscribeOn(schedulers.io)
      .map<Action> { Action.MangaReady(it.id) }
      .onErrorReturn(Action::Error)
  }

}
