/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.model

enum class LibrarySort {
  Title,
  LastRead,
  LastUpdated,
  Unread,
  TotalChapters,
  Source;
}

data class LibrarySorting(val type: LibrarySort, val isAscending: Boolean) {

  companion object
}

fun LibrarySorting.serialize(): String {
  val className = when (type) {
    LibrarySort.Title -> "Title"
    LibrarySort.LastRead -> "LastRead"
    LibrarySort.LastUpdated -> "LastUpdated"
    LibrarySort.Unread -> "Unread"
    LibrarySort.TotalChapters -> "TotalChapters"
    LibrarySort.Source -> "Source"
  }
  val order = if (isAscending) "a" else "d"
  return "$className;$order"
}

fun LibrarySorting.Companion.deserialize(serialized: String): LibrarySorting {
  if (serialized.isEmpty()) return LibrarySorting(LibrarySort.Title, true)

  val values = serialized.split(";")
  val className = values[0]
  val ascending = values[1] == "a"

  return when (className) {
    "LastRead" -> LibrarySorting(LibrarySort.LastRead, ascending)
    "LastUpdated" -> LibrarySorting(LibrarySort.LastUpdated, ascending)
    "Unread" -> LibrarySorting(LibrarySort.Unread, ascending)
    "TotalChapters" -> LibrarySorting(LibrarySort.TotalChapters, ascending)
    "Source" -> LibrarySorting(LibrarySort.Source, ascending)
    else -> LibrarySorting(LibrarySort.Title, ascending)
  }
}
