package tachiyomi.domain.library

import tachiyomi.domain.manga.Manga

data class LibraryEntry(
  val manga: Manga,
  val category: Int,
  val unread: Int
)
