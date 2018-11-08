package tachiyomi.ui.library

import tachiyomi.domain.library.LibraryCategory

data class LibraryViewState(
  val library: List<LibraryCategory> = emptyList()
)
