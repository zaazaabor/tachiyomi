/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.deeplink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tachiyomi.ui.R
import tachiyomi.ui.controller.MvpController
import tachiyomi.ui.controller.withFadeTransition
import tachiyomi.ui.screens.manga.MangaController

class MangaDeepLinkController(
  bundle: Bundle? = null
) : MvpController<MangaDeepLinkPresenter>(bundle) {

  override fun getPresenterClass() = MangaDeepLinkPresenter::class.java

  override fun getModule() = MangaDeepLinkModule(this)

  fun getMangaKey() = args.getString(MANGA_KEY)

  fun getSourceId() = args.getLong(SOURCE_KEY)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.deeplink_manga_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)
    presenter.stateObserver
      .subscribeWithView(::render)
  }

  // TODO
  private fun render(state: MangaDeepLinkViewState) {
    if (state.mangaId != null) {
      router.setRoot(MangaController(state.mangaId).withFadeTransition())
    }
  }

  companion object {
    const val MANGA_KEY = "manga_key"
    const val SOURCE_KEY = "source_key"
  }

}
