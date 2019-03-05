/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import io.reactivex.Single
import tachiyomi.core.db.Transaction
import tachiyomi.domain.category.Category
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject
import javax.inject.Provider

class ChangeMangaFavorite @Inject constructor(
  private val libraryRepository: LibraryRepository,
  private val categoryRepository: CategoryRepository,
  private val transactions: Provider<Transaction>
) {

  fun interact(manga: Manga) = Single.defer {
    val updatedManga = if (manga.favorite) {
      manga.copy(favorite = false)
    } else {
      manga.copy(favorite = true, dateAdded = System.currentTimeMillis())
    }

    val mangaIds = listOf(manga.id)
    val setCategoryOperation = if (updatedManga.favorite) {
      val categoryIds = listOf(Category.UNCATEGORIZED_ID) // TODO
      categoryRepository.setCategoriesForMangas(categoryIds, mangaIds)
    } else {
      categoryRepository.deleteCategoriesForMangas(mangaIds)
    }

    val transaction = transactions.get()
    transaction.begin()

    libraryRepository.updateFavorite(updatedManga)
      .andThen(setCategoryOperation)
      .toSingle<Result> { Result.Success }
      .doOnSuccess { transaction.commit() }
      .doFinally { transaction.end() }
      .onErrorReturn(Result::Error)
  }

  sealed class Result {
    object Success : Result()
    data class Error(val error: Throwable) : Result()
  }

}
