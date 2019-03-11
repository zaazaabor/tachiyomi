/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import io.reactivex.Observable
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class SubscribeLibraryCategory @Inject constructor(
  private val libraryRepository: LibraryRepository
) {

  fun interact(categoryId: Long): Observable<List<LibraryManga>> {
    return when (categoryId) {
      Category.ALL_ID -> libraryRepository.subscribeAll()
      Category.UNCATEGORIZED_ID -> libraryRepository.subscribeUncategorized()
      else -> libraryRepository.subscribeToCategory(categoryId)
    }
  }

}
