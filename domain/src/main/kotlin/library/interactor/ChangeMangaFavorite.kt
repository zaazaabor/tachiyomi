/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import io.reactivex.Completable
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject

class ChangeMangaFavorite @Inject constructor(private val libraryRepository: LibraryRepository) {

  fun interact(manga: Manga) = Completable.defer {
    val updatedManga = if (manga.favorite) {
      manga.copy(favorite = false)
    } else {
      manga.copy(favorite = true, dateAdded = System.currentTimeMillis())
    }
    libraryRepository.updateFavorite(updatedManga)
  }
}
