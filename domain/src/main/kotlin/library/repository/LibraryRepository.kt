/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.library.model.LibraryManga

interface LibraryRepository {

  fun subscribeAll(): Flow<List<LibraryManga>>

  fun subscribeUncategorized(): Flow<List<LibraryManga>>

  fun subscribeToCategory(categoryId: Long): Flow<List<LibraryManga>>

  fun findAll(): List<LibraryManga>

  fun findUncategorized(): List<LibraryManga>

  fun findForCategory(categoryId: Long): List<LibraryManga>

  fun findFavoriteSourceIds(): List<Long>

}
