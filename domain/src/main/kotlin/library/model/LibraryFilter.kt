/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.model

enum class LibraryFilter {
  Downloaded,
  Unread,
  Completed,
  AllCategory;

  companion object
}

fun LibraryFilter.serialize(): String {
  return when (this) {
    LibraryFilter.Downloaded -> "Downloaded"
    LibraryFilter.Unread -> "Unread"
    LibraryFilter.Completed -> "Completed"
    LibraryFilter.AllCategory -> "AllCategory"
  }
}

fun LibraryFilter.Companion.deserialize(serialized: String): LibraryFilter? {
  return when (serialized) {
    "Downloaded" -> LibraryFilter.Downloaded
    "Unread" -> LibraryFilter.Unread
    "Completed" -> LibraryFilter.Completed
    "AllCategory" -> LibraryFilter.AllCategory
    else -> null
  }
}

fun List<LibraryFilter>.serialize(): String {
  return joinToString(";") { it.serialize() }
}

fun LibraryFilter.Companion.deserializeList(serialized: String): List<LibraryFilter> {
  return serialized.split(";").mapNotNull { LibraryFilter.deserialize(it) }
}
