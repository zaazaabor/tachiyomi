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
import tachiyomi.domain.category.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategories @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(categoryId: Long): Completable {
    if (categoryId <= 0) {
      return Completable.complete()
    }

    return categoryRepository.deleteCategory(categoryId)
      .onErrorComplete()
  }

  fun interact(category: Category): Completable {
    return interact(category.id)
  }

  fun interact(categoryIds: Collection<Long>): Completable {
    val safeCategoryIds = categoryIds.filter { it > 0 }
    if (safeCategoryIds.isEmpty()) {
      return Completable.complete()
    }

    return categoryRepository.deleteCategories(safeCategoryIds)
      .onErrorComplete()
  }

}
