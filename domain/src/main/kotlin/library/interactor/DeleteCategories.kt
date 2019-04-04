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
import tachiyomi.domain.library.prefs.LibraryPreferences
import tachiyomi.domain.library.repository.CategoryRepository
import tachiyomi.domain.library.updater.LibraryUpdater
import javax.inject.Inject

class DeleteCategories @Inject constructor(
  private val categoryRepository: CategoryRepository,
  private val libraryPreferences: LibraryPreferences,
  private val libraryUpdater: LibraryUpdater
) {

  fun interact(categoryIds: Collection<Long>) = Single.fromCallable {
    val safeCategoryIds = categoryIds.filter { it > 0 }
    if (safeCategoryIds.isEmpty()) {
      return@fromCallable Result.NothingToDelete
    }

    categoryRepository.delete(safeCategoryIds)

    if (libraryPreferences.defaultCategory().get() in safeCategoryIds) {
      libraryPreferences.defaultCategory().delete()
    }
    for (id in safeCategoryIds) {
      libraryUpdater.unscheduleAll(id)
    }

    Result.Success as Result
  }.onErrorReturn(Result::InternalError)

  fun interact(categoryId: Long): Single<Result> {
    return interact(listOf(categoryId))
  }

  fun interact(category: Category): Single<Result> {
    return interact(category.id)
  }

  sealed class Result {
    object Success : Result()
    object NothingToDelete : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
