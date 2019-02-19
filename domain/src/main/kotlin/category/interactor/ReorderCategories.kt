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

class ReorderCategory @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(categories: List<Category>): Completable {
    return categoryRepository.reorderCategories(categories)
  }

  fun interact(categoryId: Long, newPosition: Int): Completable {
    return categoryRepository.getCategories()
      .take(1)
      .flatMapCompletable { categories ->
        val currPosition = categories.indexOfFirst { it.id == categoryId }
        if (currPosition == newPosition || currPosition == -1) {
          return@flatMapCompletable Completable.complete()
        }

        val mutCategories = categories.toMutableList()
        val category = mutCategories.removeAt(currPosition)
        mutCategories.add(newPosition, category)

        interact(mutCategories)
      }
  }

  fun interact(category: Category, newPosition: Int): Completable {
    return interact(category.id, newPosition)
  }

}
