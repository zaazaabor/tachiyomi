/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.category.interactor

import io.reactivex.Completable
import tachiyomi.domain.category.Category
import tachiyomi.domain.category.exception.CategoryAlreadyExists
import tachiyomi.domain.category.exception.EmptyCategoryName
import tachiyomi.domain.category.repository.CategoryRepository
import javax.inject.Inject

class RenameCategory @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(categoryId: Long, newName: String): Completable {
    if (newName.isBlank()) {
      return Completable.error(EmptyCategoryName())
    }
    return categoryRepository.getCategories()
      .take(1)
      .flatMapCompletable { categories ->
        val categoryWithSameName = categories.find { newName.equals(it.name, ignoreCase = true) }

        // Allow to rename if it doesn't exist or it's the same category
        if (categoryWithSameName == null || categoryWithSameName.id == categoryId) {
          categoryRepository.renameCategory(categoryId, newName)
        } else {
          Completable.error(CategoryAlreadyExists(newName))
        }
      }
  }

  fun interact(category: Category, newName: String): Completable {
    return interact(category.id, newName)
  }
}
