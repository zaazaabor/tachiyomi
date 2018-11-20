/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.model

sealed class LibrarySort {

  abstract val ascending: Boolean

  data class Title(override val ascending: Boolean) : LibrarySort()
  data class LastRead(override val ascending: Boolean) : LibrarySort()
  data class LastUpdated(override val ascending: Boolean) : LibrarySort()
  data class Unread(override val ascending: Boolean) : LibrarySort()
  data class TotalChapters(override val ascending: Boolean) : LibrarySort()
  data class Source(override val ascending: Boolean) : LibrarySort()

  companion object
}

fun LibrarySort.serialize(): String {
  val className = when (this) {
    is LibrarySort.Title -> "Title"
    is LibrarySort.LastRead -> "LastRead"
    is LibrarySort.LastUpdated -> "LastUpdated"
    is LibrarySort.Unread -> "Unread"
    is LibrarySort.TotalChapters -> "TotalChapters"
    is LibrarySort.Source -> "Source"
  }
  val order = if (ascending) "a" else "d"
  return "$className;$order"
}

fun LibrarySort.Companion.deserialize(serialized: String): LibrarySort {
  val values = serialized.split(";")
  val className = values[0]
  val ascending = values[1] == "a"

  return when (className) {
    "LastRead" -> LibrarySort.LastRead(ascending)
    "LastUpdated" -> LibrarySort.LastUpdated(ascending)
    "Unread" -> LibrarySort.Unread(ascending)
    "TotalChapters" -> LibrarySort.TotalChapters(ascending)
    "Source" -> LibrarySort.Source(ascending)
    else -> LibrarySort.Title(ascending)
  }
}
