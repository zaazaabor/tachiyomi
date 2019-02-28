/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.Manga

interface LibraryRepository {

  fun getLibraryMangas(): Flowable<List<LibraryManga>>

  fun getFavoriteSourceIds(): Single<List<Long>>

  fun updateFavorite(manga: Manga): Completable
}
