/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.category.interactor

import io.reactivex.Completable
import io.reactivex.Single
import tachiyomi.core.stdlib.Optional
import tachiyomi.domain.category.model.CategoryUpdate
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.library.updater.LibraryUpdater
import javax.inject.Inject

class SetCategoryUpdateInterval @Inject constructor(
  private val categoryRepository: CategoryRepository,
  private val libraryUpdater: LibraryUpdater
) {

  fun interact(categoryId: Long, intervalInHours: Int) = Single.defer {
    val update = CategoryUpdate(
      id = categoryId,
      updateInterval = Optional.of(intervalInHours)
    )
    val scheduleOperation = Completable.fromAction {
      if (intervalInHours > 0) {
        libraryUpdater.schedule(categoryId, LibraryUpdater.Target.Chapters, intervalInHours)
      } else {
        libraryUpdater.unschedule(categoryId, LibraryUpdater.Target.Chapters)
      }
    }

    categoryRepository.savePartial(update)
      .andThen(scheduleOperation)
      .toSingle<Result> { Result.Success }
      .onErrorReturn(Result::InternalError)
  }

  sealed class Result {
    object Success : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
