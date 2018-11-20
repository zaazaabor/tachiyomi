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

class ReorderCategories @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(categories: List<Category>): Completable {
    return categoryRepository.reorderCategories(categories)
      .onErrorComplete()
  }

  fun interact(from: Long, to: Long): Completable {
    return categoryRepository.getCategories()
      .take(1)
      .flatMapCompletable { categories ->
        val fromPosition = categories.indexOfFirst { it.id == from }
        val toPosition = categories.indexOfFirst { it.id == to }

        val mutCategories = categories.toMutableList()
        val aux = categories[fromPosition]

        mutCategories[fromPosition] = mutCategories[toPosition]
        mutCategories[toPosition] = aux

        interact(mutCategories)
      }
  }

  fun interact(from: Category, to: Category): Completable {
    return interact(from.id, to.id)
  }

}
