/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.library

import tachiyomi.domain.library.model.LibraryCategory
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibrarySort

data class ViewState(
  val library: List<LibraryCategory> = emptyList(),
  val filters: List<LibraryFilter> = emptyList(),
  val sort: LibrarySort = LibrarySort.Title(true)
)
