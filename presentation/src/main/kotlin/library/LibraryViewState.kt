/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.LibrarySorting

data class ViewState(
  val categories: List<Category> = emptyList(),
  val selectedCategory: Category? = null,
  val library: List<LibraryManga> = emptyList(),
  val filters: List<LibraryFilter> = emptyList(),
  val sorting: LibrarySorting = LibrarySorting(LibrarySort.Title, true),
  val selectedManga: Set<Long> = emptySet(),
  val showUpdatingCategory: Boolean = false,
  val showQuickCategories: Boolean = false,
  val sheetVisible: Boolean = false
)
