/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.category.interactor

import io.reactivex.Completable
import tachiyomi.domain.category.exception.CategoryAlreadyExists
import tachiyomi.domain.category.exception.EmptyCategoryName
import tachiyomi.domain.category.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryWithName @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(name: String): Completable {
    if (name.isBlank()) {
      return Completable.error(EmptyCategoryName())
    }
    return categoryRepository.getCategories()
      .take(1)
      .flatMapCompletable { categories ->
        if (categories.none { name.equals(it.name, ignoreCase = true) }) {
          val nextOrder = categories.maxBy { it.order }?.order?.plus(1) ?: 0
          categoryRepository.createCategory(name, nextOrder)
        } else {
          Completable.error(CategoryAlreadyExists(name))
        }
      }
  }
}
