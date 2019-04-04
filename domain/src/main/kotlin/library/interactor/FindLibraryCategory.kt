/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import io.reactivex.Single
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class FindLibraryCategory @Inject constructor(
  private val libraryRepository: LibraryRepository
) {

  fun interact(categoryId: Long) = Single.fromCallable {
    when (categoryId) {
      Category.ALL_ID -> libraryRepository.findAll()
      Category.UNCATEGORIZED_ID -> libraryRepository.findUncategorized()
      else -> libraryRepository.findToCategory(categoryId)
    }
  }

}
