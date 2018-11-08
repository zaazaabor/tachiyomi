package tachiyomi.ui.deeplink

import tachiyomi.ui.deeplink.MangaDeepLinkAction as Action
import tachiyomi.ui.deeplink.MangaDeepLinkViewState as ViewState

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
