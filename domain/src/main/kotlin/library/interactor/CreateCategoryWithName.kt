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
import tachiyomi.domain.library.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryWithName @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(name: String): Single<Result> {
    if (name.isBlank()) {
      return Single.just(Result.EmptyCategoryNameError)
    }
    return categoryRepository.subscribe()
      .firstOrError()
      .flatMap<Result> { categories ->
        if (categories.none { name.equals(it.name, ignoreCase = true) }) {
          val nextOrder = categories.maxBy { it.order }?.order?.plus(1) ?: 0

          val newCategory = Category(
            id = -1,
            name = name,
            order = nextOrder
          )
          categoryRepository.save(newCategory)
            .toSingle { Result.Success }
        } else {
          Single.just(Result.CategoryAlreadyExistsError(name))
        }
      }
      .onErrorReturn(Result::InternalError)
  }

  sealed class Result {
    object Success : Result()
    object EmptyCategoryNameError : Result()
    data class CategoryAlreadyExistsError(val name: String) : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
