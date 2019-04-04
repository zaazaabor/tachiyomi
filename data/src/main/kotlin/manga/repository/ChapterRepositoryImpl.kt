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
import io.reactivex.Observable
import tachiyomi.core.db.asBlocking
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

  override fun subscribeForManga(mangaId: Long): Observable<List<Chapter>> {
    return preparedChapters(mangaId)
      .asRxFlowable(BackpressureStrategy.LATEST)
      .distinctUntilChanged() // TODO do we want to run a distinct?
      .toObservable()
  }

  override fun subscribe(chapterId: Long): Observable<Optional<Chapter>> {
    return preparedChapter(chapterId).asRxFlowable(BackpressureStrategy.LATEST)
      .distinctUntilChanged()
      .map { it.toOptional() }
      .toObservable()
  }

  override fun findForManga(mangaId: Long): List<Chapter> {
    return preparedChapters(mangaId).asBlocking()
  }

  override fun find(chapterId: Long): Chapter? {
    return preparedChapter(chapterId).asBlocking()
  }

  override fun find(chapterKey: String, mangaId: Long): Chapter? {
    return storio.get()
      .`object`(Chapter::class.java)
      .withQuery(Query.builder()
        .table(ChapterTable.TABLE)
        .where("${ChapterTable.COL_KEY} = ? AND ${ChapterTable.COL_MANGA_ID} = ?")
        .whereArgs(chapterKey, mangaId)
        .build())
      .prepare()
      .asBlocking()
  }

  override fun save(chapters: List<Chapter>) {
    storio.put()
      .objects(chapters)
      .prepare()
      .asBlocking()
  }

  override fun savePartial(update: List<ChapterUpdate>) {
    storio.put()
      .objects(update)
      .withPutResolver(ChapterUpdatePutResolver)
      .prepare()
      .asBlocking()
  }

  override fun saveNewOrder(chapters: List<Chapter>) {
    storio.put()
      .objects(chapters)
      .withPutResolver(ChapterSourceOrderPutResolver)
      .prepare()
      .asBlocking()
  }

  override fun delete(chapterId: Long) {
    storio.delete()
      .withId(ChapterTable.TABLE, ChapterTable.COL_ID, chapterId)
      .prepare()
      .asBlocking()
  }

  override fun delete(chapterIds: List<Long>) {
    storio.delete()
      .withIds(ChapterTable.TABLE, ChapterTable.COL_ID, chapterIds)
      .prepare()
      .asBlocking()
  }

}
