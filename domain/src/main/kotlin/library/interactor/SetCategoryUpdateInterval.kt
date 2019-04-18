/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.withContext
import tachiyomi.core.rx.CoroutineDispatchers
import tachiyomi.core.stdlib.Optional
import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.repository.CategoryRepository
import tachiyomi.domain.library.updater.LibraryUpdateScheduler
import tachiyomi.domain.library.updater.LibraryUpdater
import javax.inject.Inject

class SetCategoryUpdateInterval @Inject constructor(
  private val categoryRepository: CategoryRepository,
  private val libraryScheduler: LibraryUpdateScheduler,
  private val dispatchers: CoroutineDispatchers
) {

  suspend fun await(categoryId: Long, intervalInHours: Int): Result {
    val update = CategoryUpdate(
      id = categoryId,
      updateInterval = Optional.of(intervalInHours)
    )
    try {
      withContext(dispatchers.io) { categoryRepository.savePartial(update) }

      if (intervalInHours > 0) {
        libraryScheduler.schedule(categoryId, LibraryUpdater.Target.Chapters, intervalInHours)
      } else {
        libraryScheduler.unschedule(categoryId, LibraryUpdater.Target.Chapters)
      }
    } catch (e: Exception) {
      return Result.InternalError(e)
    }

    return Result.Success
  }

  sealed class Result {
    object Success : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
