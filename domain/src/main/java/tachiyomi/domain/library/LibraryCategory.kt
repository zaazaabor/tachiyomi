package tachiyomi.domain.library

import tachiyomi.domain.category.Category

data class LibraryCategory(
  val category: Category,
  val entries: List<LibraryEntry>
)
