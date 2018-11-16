package tachiyomi.domain.library.model

import tachiyomi.domain.category.Category

data class LibraryCategory(
  val category: Category,
  val mangas: List<LibraryManga>
)
