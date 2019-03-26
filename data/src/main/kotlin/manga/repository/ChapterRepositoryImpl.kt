/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.repository

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetListOfObjects
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetObject
import com.pushtorefresh.storio3.sqlite.queries.Query
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import tachiyomi.core.db.asImmediateCompletable
import tachiyomi.core.db.asImmediateMaybe
import tachiyomi.core.db.asImmediateSingle
import tachiyomi.core.db.toOptional
import tachiyomi.core.db.withId
import tachiyomi.core.db.withIds
import tachiyomi.core.stdlib.Optional
import tachiyomi.data.manga.sql.ChapterSourceOrderPutResolver
import tachiyomi.data.manga.sql.ChapterTable
import tachiyomi.data.manga.sql.ChapterUpdatePutResolver
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.ChapterUpdate
import tachiyomi.domain.manga.repository.ChapterRepository
import javax.inject.Inject

class ChapterRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : ChapterRepository {

  private fun preparedChapter(chapterId: Long): PreparedGetObject<Chapter> {
    return storio.get()
      .`object`(Chapter::class.java)
      .withQuery(Query.builder()
        .table(ChapterTable.TABLE)
        .where("${ChapterTable.COL_ID} = ?")
        .whereArgs(chapterId)
        .build())
      .prepare()
  }

  private fun preparedChapters(mangaId: Long): PreparedGetListOfObjects<Chapter> {
    return storio.get()
      .listOfObjects(Chapter::class.java)
      .withQuery(Query.builder()
        .table(ChapterTable.TABLE)
        .where("${ChapterTable.COL_MANGA_ID} = ?")
        .whereArgs(mangaId)
        .build())
      .prepare()
  }

  override fun subscribeForManga(mangaId: Long): Flowable<List<Chapter>> {
    return preparedChapters(mangaId)
      .asRxFlowable(BackpressureStrategy.LATEST)
      .distinctUntilChanged() // TODO do we want to run a distinct?
  }

  override fun subscribe(chapterId: Long): Flowable<Optional<Chapter>> {
    return preparedChapter(chapterId).asRxFlowable(BackpressureStrategy.LATEST)
      .distinctUntilChanged()
      .map { it.toOptional() }
  }

  override fun findForManga(mangaId: Long): Single<List<Chapter>> {
    return preparedChapters(mangaId).asImmediateSingle()
  }

  override fun find(chapterId: Long): Maybe<Chapter> {
    return preparedChapter(chapterId).asImmediateMaybe()
  }

  override fun find(chapterKey: String, mangaId: Long): Maybe<Chapter> {
    return storio.get()
      .`object`(Chapter::class.java)
      .withQuery(Query.builder()
        .table(ChapterTable.TABLE)
        .where("${ChapterTable.COL_KEY} = ? AND ${ChapterTable.COL_MANGA_ID} = ?")
        .whereArgs(chapterKey, mangaId)
        .build())
      .prepare()
      .asImmediateMaybe()
  }

  override fun save(chapters: List<Chapter>): Completable {
    return storio.put()
      .objects(chapters)
      .prepare()
      .asImmediateCompletable()
  }

  override fun savePartial(update: List<ChapterUpdate>): Completable {
    return storio.put()
      .objects(update)
      .withPutResolver(ChapterUpdatePutResolver)
      .prepare()
      .asImmediateCompletable()
  }

  override fun saveNewOrder(chapters: List<Chapter>): Completable {
    return storio.put()
      .objects(chapters)
      .withPutResolver(ChapterSourceOrderPutResolver)
      .prepare()
      .asImmediateCompletable()
  }

  override fun delete(chapterId: Long): Completable {
    return storio.delete()
      .withId(ChapterTable.TABLE, ChapterTable.COL_ID, chapterId)
      .prepare()
      .asImmediateCompletable()
  }

  override fun delete(chapterIds: List<Long>): Completable {
    return storio.delete()
      .withIds(ChapterTable.TABLE, ChapterTable.COL_ID, chapterIds)
      .prepare()
      .asImmediateCompletable()
  }

}
