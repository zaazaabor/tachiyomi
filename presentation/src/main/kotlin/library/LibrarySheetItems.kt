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
import tachiyomi.domain.library.model.LibrarySort

sealed class LibrarySheetItem

object CategoriesHeader : LibrarySheetItem()

data class CategoryItem(val category: Category, val isSelected: Boolean) : LibrarySheetItem()

object FiltersHeader : LibrarySheetItem()

data class FilterItem(val filter: LibraryFilter, val isEnabled: Boolean) : LibrarySheetItem()

object SortHeader : LibrarySheetItem()

data class SortItem(val sort: LibrarySort, val isSelected: Boolean) : LibrarySheetItem()

// TODO remaining items
