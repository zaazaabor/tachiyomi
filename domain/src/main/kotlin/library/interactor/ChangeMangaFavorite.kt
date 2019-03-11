/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import io.reactivex.Completable
import io.reactivex.Single
import tachiyomi.core.db.Transaction
import tachiyomi.core.stdlib.Optional
import tachiyomi.domain.category.repository.MangaCategoryRepository
import tachiyomi.domain.library.prefs.LibraryPreferences
import tachiyomi.domain.library.repository.LibraryCovers
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject
import javax.inject.Provider

class ChangeMangaFavorite @Inject constructor(
  private val mangaRepository: MangaRepository,
  private val mangaCategoryRepository: MangaCategoryRepository,
  private val libraryPreferences: LibraryPreferences,
  private val libraryCovers: LibraryCovers,
  private val transactions: Provider<Transaction>,
  private val setCategoriesForMangas: Provider<SetCategoriesForMangas>
) {

  fun interact(manga: Manga) = Single.defer {
    val now = System.currentTimeMillis()
    val nowFavorite = !manga.favorite
    val update = if (nowFavorite) {
      MangaUpdate(
        id = manga.id,
        favorite = Optional.of(true),
        dateAdded = Optional.of(now)
      )
    } else {
      MangaUpdate(id = manga.id, favorite = Optional.of(false))
    }

    val mangaIds = listOf(manga.id)
    val setCategoryOperation = if (nowFavorite) {
      val defaultCategory = libraryPreferences.defaultCategory().get()
//      val mangaCategories = mangaIds.map { mangaId ->
//        MangaCategory(mangaId, defaultCategory)
//      }
      setCategoriesForMangas.get().interact(listOf(defaultCategory), mangaIds)
        .flatMapCompletable { result ->
          when (result) {
            SetCategoriesForMangas.Result.Success -> Completable.complete()
            is SetCategoriesForMangas.Result.InternalError -> Completable.error(result.error)
          }
        }
    } else {
      mangaCategoryRepository.deleteForMangas(mangaIds)
    }

    val transaction = transactions.get()
    transaction.begin()

    mangaRepository.savePartial(update)
      .andThen(setCategoryOperation)
      .toSingle<Result> { Result.Success }
      .doOnSuccess {
        if (!nowFavorite) {
          libraryCovers.delete(manga.id)
        }
      }
      .doOnSuccess { transaction.commit() }
      .doFinally { transaction.end() }
      .onErrorReturn(Result::InternalError)
  }

  sealed class Result {
    object Success : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
