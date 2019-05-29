/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.deeplink

import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.deeplink.ChapterDeepLinkAction as Action
import tachiyomi.ui.deeplink.ChapterDeepLinkViewState as ViewState

sealed class ChapterDeepLinkAction {

  data class MangaReady(val manga: Manga) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(manga = manga)
  }

  data class ChapterReady(val chapter: Chapter) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(loading = false, chapter = chapter)
  }

  data class Error(val error: Throwable) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(loading = false, error = error)
  }

  open fun reduce(state: ViewState) = state
}
