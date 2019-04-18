/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import tachiyomi.core.rx.CoroutineDispatchers
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class GetLibraryCategory @Inject constructor(
  private val libraryRepository: LibraryRepository,
  private val dispatchers: CoroutineDispatchers
) {

  fun execute(categoryId: Long): List<LibraryManga> {
    return when (categoryId) {
      Category.ALL_ID -> libraryRepository.findAll()
      Category.UNCATEGORIZED_ID -> libraryRepository.findUncategorized()
      else -> libraryRepository.findForCategory(categoryId)
    }
  }

  suspend fun await(categoryId: Long): List<LibraryManga> {
    return withContext(dispatchers.io) {
      when (categoryId) {
        Category.ALL_ID -> libraryRepository.findAll()
        Category.UNCATEGORIZED_ID -> libraryRepository.findUncategorized()
        else -> libraryRepository.findForCategory(categoryId)
      }
    }
  }

  fun subscribe(categoryId: Long): Flow<List<LibraryManga>> {
    return when (categoryId) {
      Category.ALL_ID -> libraryRepository.subscribeAll()
      Category.UNCATEGORIZED_ID -> libraryRepository.subscribeUncategorized()
      else -> libraryRepository.subscribeToCategory(categoryId)
    }
  }

}
