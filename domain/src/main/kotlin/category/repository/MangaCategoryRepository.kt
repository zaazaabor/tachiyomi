/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.category.repository

import io.reactivex.Completable
import tachiyomi.domain.category.model.MangaCategory

interface MangaCategoryRepository {

  fun save(mangaCategory: MangaCategory): Completable

  fun save(mangaCategories: Collection<MangaCategory>): Completable

  fun deleteForManga(mangaId: Long): Completable

  fun deleteForMangas(mangaIds: Collection<Long>): Completable

}
