/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.deeplink

import tachiyomi.ui.deeplink.MangaDeepLinkViewState as ViewState
import tachiyomi.ui.deeplink.MangaDeepLinkAction as Action

sealed class MangaDeepLinkAction {

  data class MangaReady(val mangaId: Long) : Action() {
    override fun reduce(state: ViewState): ViewState {
      return state.copy(loading = false, mangaId = mangaId)
    }
  }

  data class Error(val error: Throwable?) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(loading = false, error = error)
  }

  open fun reduce(state: ViewState) = state
}
