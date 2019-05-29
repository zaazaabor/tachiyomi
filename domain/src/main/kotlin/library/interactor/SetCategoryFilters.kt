/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import tachiyomi.core.util.Optional
import tachiyomi.core.util.CoroutineDispatchers
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.prefs.LibraryPreferences
import tachiyomi.domain.library.repository.CategoryRepository
import timber.log.Timber
import timber.log.warn
import javax.inject.Inject

class SetCategoryFilters @Inject constructor(
  private val categoryRepository: CategoryRepository,
  private val libraryPreferences: LibraryPreferences,
  private val dispatchers: CoroutineDispatchers
) {

  suspend fun await(category: Category, filters: List<LibraryFilter>): Result {
    return withContext(NonCancellable) {
      try {
        if (category.useOwnFilters) {
          val update = CategoryUpdate(category.id, filters = Optional.of(filters))
          withContext(dispatchers.io) { categoryRepository.savePartial(update) }
        } else {
          libraryPreferences.filters().set(filters)
        }
        Result.Success
      } catch (e: Exception) {
        Timber.warn(e) { e.message.orEmpty() }
        Result.InternalError(e)
      }
    }
  }

  sealed class Result {
    object Success : Result()
    data class InternalError(val error: Exception) : Result()
  }

}
