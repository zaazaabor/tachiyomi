package tachiyomi.ui.deeplink

import tachiyomi.domain.chapter.model.Chapter
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
