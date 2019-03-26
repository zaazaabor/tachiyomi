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
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.ChapterUpdate

interface ChapterRepository {

  fun subscribeForManga(mangaId: Long): Flowable<List<Chapter>>

  fun subscribe(chapterId: Long): Flowable<Optional<Chapter>>

  fun findForManga(mangaId: Long): Single<List<Chapter>>

  fun find(chapterId: Long): Maybe<Chapter>

  fun find(chapterKey: String, mangaId: Long): Maybe<Chapter>

  fun save(chapters: List<Chapter>): Completable

  fun savePartial(update: List<ChapterUpdate>): Completable

  fun saveNewOrder(chapters: List<Chapter>): Completable

  fun delete(chapterId: Long): Completable

  fun delete(chapterIds: List<Long>): Completable

}
