/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.category.interactor

import io.reactivex.Single
import tachiyomi.core.stdlib.Optional
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.model.CategoryUpdate
import tachiyomi.domain.category.repository.CategoryRepository
import javax.inject.Inject

class RenameCategory @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(categoryId: Long, newName: String): Single<Result> {
    if (newName.isBlank()) {
      return Single.just(Result.EmptyNameError)
    }
    return categoryRepository.subscribe()
      .firstOrError()
      .flatMap { categories ->
        val category = categories.find { it.id == categoryId }
          ?: return@flatMap Single.just(Result.NotFoundError)

        if (category.isSystemCategory) {
          return@flatMap Single.just(Result.CantBeRenamedError)
        }

        // Allow to rename if it doesn't exist or it's the same category
        val categoryWithSameName = categories.find { it.name == newName }
        if (categoryWithSameName != null && categoryWithSameName.id != categoryId) {
          return@flatMap Single.just(Result.NameAlreadyExistsError)
        }

        val update = CategoryUpdate(
          id = categoryId,
          name = Optional.of(newName)
        )

        categoryRepository.savePartial(update)
          .andThen(Single.just<Result>(Result.Success))
          .onErrorReturn(Result::InternalError)
      }
  }

  fun interact(category: Category, newName: String): Single<Result> {
    return interact(category.id, newName)
  }

  sealed class Result {
    object Success : Result()
    object EmptyNameError : Result()
    object NameAlreadyExistsError : Result()
    object CantBeRenamedError : Result()
    object NotFoundError : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
