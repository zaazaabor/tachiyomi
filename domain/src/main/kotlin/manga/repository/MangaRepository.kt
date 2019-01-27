/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import tachiyomi.core.stdlib.Optional
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.model.MangaInfo

interface MangaRepository {

  fun setFlags(manga: Manga, flags: Int): Completable

  fun subscribeManga(mangaId: Long): Flowable<Optional<Manga>>

  fun subscribeManga(key: String, sourceId: Long): Flowable<Optional<Manga>>

  fun getManga(mangaId: Long): Maybe<Manga>

  fun getManga(key: String, sourceId: Long): Maybe<Manga>

  fun updateMangaDetails(manga: Manga): Completable

  fun saveAndReturnNewManga(manga: MangaInfo, sourceId: Long): Single<Manga>

  fun deleteNonFavorite(): Completable
}
