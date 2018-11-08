package tachiyomi.ui.deeplink

import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.model.Manga

data class ChapterDeepLinkViewState(
  val loading: Boolean = true,
  val manga: Manga? = null,
  val chapter: Chapter? = null,
  val error: Throwable? = null
)
