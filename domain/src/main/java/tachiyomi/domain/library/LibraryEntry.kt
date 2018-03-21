package tachiyomi.domain.library

import tachiyomi.domain.manga.model.Manga

data class LibraryEntry(
  val manga: Manga,
  val category: Long,
  val unread: Int
)
