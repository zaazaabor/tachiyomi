/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import io.reactivex.Single
import tachiyomi.domain.category.repository.CategoryRepository
import javax.inject.Inject

class SetCategoriesForMangas @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(categoryIds: Collection<Long>, mangaIds: Collection<Long>): Single<Result> {
    return Single.just(Result.Success)

    //TODO
//    return categoryRepository.setCategoriesForMangas(categoryIds, mangaIds)
//      .toSingle<Result> { Result.Success }
//      .onErrorReturn { Result.Error(it) }
  }

  sealed class Result {
    object Success : Result()
    data class Error(val error: Throwable) : Result()
  }

}
