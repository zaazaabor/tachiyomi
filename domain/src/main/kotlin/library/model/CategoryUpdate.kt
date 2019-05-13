/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.model

import tachiyomi.core.stdlib.Optional

data class CategoryUpdate(
  val id: Long,
  val name: Optional<String> = Optional.None,
  val order: Optional<Int> = Optional.None,
  val updateInterval: Optional<Int> = Optional.None,
  val useOwnFilters: Optional<Boolean> = Optional.None,
  val filters: Optional<List<LibraryFilter>> = Optional.None,
  val sort: Optional<LibrarySorting> = Optional.None
)
