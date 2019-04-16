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
import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.repository.CategoryRepository
import tachiyomi.domain.library.updater.LibraryUpdateScheduler
import tachiyomi.domain.library.updater.LibraryUpdater
import javax.inject.Inject

class SetCategoryUpdateInterval @Inject constructor(
  private val categoryRepository: CategoryRepository,
  private val libraryScheduler: LibraryUpdateScheduler
) {

  fun interact(categoryId: Long, intervalInHours: Int) = Single.fromCallable {
    val update = CategoryUpdate(
      id = categoryId,
      updateInterval = Optional.of(intervalInHours)
    )
    categoryRepository.savePartial(update)

    if (intervalInHours > 0) {
      libraryScheduler.schedule(categoryId, LibraryUpdater.Target.Chapters, intervalInHours)
    } else {
      libraryScheduler.unschedule(categoryId, LibraryUpdater.Target.Chapters)
    }

    Result.Success as Result
  }.onErrorReturn(Result::InternalError)

  sealed class Result {
    object Success : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
