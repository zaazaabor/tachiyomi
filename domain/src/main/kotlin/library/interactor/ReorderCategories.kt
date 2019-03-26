/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import io.reactivex.Single
import tachiyomi.core.stdlib.Optional
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.repository.CategoryRepository
import javax.inject.Inject

class ReorderCategory @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(categoryId: Long, newPosition: Int): Single<Result> {
    return categoryRepository.subscribe()
      .firstOrError()
      .flatMap { categories ->
        val currPosition = categories.indexOfFirst { it.id == categoryId }
        if (currPosition == newPosition || currPosition == -1) {
          return@flatMap Single.just(Result.Unchanged)
        }

        val reorderedCategories = categories.toMutableList()
        val movedCategory = reorderedCategories.removeAt(currPosition)
        reorderedCategories.add(newPosition, movedCategory)

        val updates = reorderedCategories.mapIndexed { index, category ->
          CategoryUpdate(
            id = category.id,
            order = Optional.of(index)
          )
        }

        categoryRepository.savePartial(updates)
          .toSingle<Result> { Result.Success }
      }
      .onErrorReturn(Result::InternalError)
  }

  fun interact(category: Category, newPosition: Int): Single<Result> {
    return interact(category.id, newPosition)
  }

  sealed class Result {
    object Success : Result()
    object Unchanged : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
