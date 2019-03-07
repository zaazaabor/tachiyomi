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
import tachiyomi.core.stdlib.Optional
import tachiyomi.domain.category.model.MangaCategory
import tachiyomi.domain.category.repository.MangaCategoryRepository
import tachiyomi.domain.library.prefs.LibraryPreferences
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject
import javax.inject.Provider

class ChangeMangaFavorite @Inject constructor(
  private val mangaRepository: MangaRepository,
  private val mangaCategoryRepository: MangaCategoryRepository,
  private val libraryPreferences: LibraryPreferences,
  private val transactions: Provider<Transaction>
) {

  fun interact(manga: Manga) = Single.defer {
    val update = if (manga.favorite) {
      MangaUpdate(id = manga.id, favorite = Optional.of(false))
    } else {
      MangaUpdate(
        id = manga.id,
        favorite = Optional.of(true),
        dateAdded = Optional.of(System.currentTimeMillis())
      )
    }

    val mangaIds = listOf(manga.id)
    val setCategoryOperation = if (!manga.favorite) {
      val defaultCategory = libraryPreferences.defaultCategory().get()
      val mangaCategories = mangaIds.map { mangaId ->
        MangaCategory(mangaId, defaultCategory)
      }
      mangaCategoryRepository.save(mangaCategories)
    } else {
      mangaCategoryRepository.deleteForMangas(mangaIds)
    }

    val transaction = transactions.get()
    transaction.begin()

    mangaRepository.savePartial(update)
      .andThen(setCategoryOperation)
      .toSingle<Result> { Result.Success }
      .doOnSuccess { transaction.commit() }
      .doFinally { transaction.end() }
      .onErrorReturn(Result::InternalError)
  }

  sealed class Result {
    object Success : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
