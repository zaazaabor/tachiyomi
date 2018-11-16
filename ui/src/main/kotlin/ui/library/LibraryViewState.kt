package tachiyomi.ui.library

import tachiyomi.domain.library.model.LibraryCategory
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibrarySort

data class LibraryViewState(
  val library: List<LibraryCategory> = emptyList(),
  val filters: List<LibraryFilter> = emptyList(),
  val sort: LibrarySort = LibrarySort.Title(true)
)
