/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.content.Context
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.ui.R

fun LibrarySort.getName(context: Context): String {
  return context.getString(when (this) {
    LibrarySort.Title -> R.string.library_sort_title
    LibrarySort.LastRead -> R.string.library_sort_lastread
    LibrarySort.LastUpdated -> R.string.library_sort_lastupdated
    LibrarySort.Unread -> R.string.library_sort_unread
    LibrarySort.TotalChapters -> R.string.library_sort_totalchapters
    LibrarySort.Source -> R.string.library_sort_source
  })
}
