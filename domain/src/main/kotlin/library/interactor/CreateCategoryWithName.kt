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

  fun interact(name: String): Single<Result> = Single.fromCallable {
    if (name.isBlank()) {
      return@fromCallable Result.EmptyCategoryNameError
    }
    val categories = categoryRepository.findAll()
    if (categories.any { name.equals(it.name, ignoreCase = true) }) {
      return@fromCallable Result.CategoryAlreadyExistsError(name)
    }

    val nextOrder = categories.maxBy { it.order }?.order?.plus(1) ?: 0
    val newCategory = Category(
      id = -1,
      name = name,
      order = nextOrder
    )
    categoryRepository.save(newCategory)
    Result.Success
  }.onErrorReturn(Result::InternalError)

  sealed class Result {
    object Success : Result()
    object EmptyCategoryNameError : Result()
    data class CategoryAlreadyExistsError(val name: String) : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
