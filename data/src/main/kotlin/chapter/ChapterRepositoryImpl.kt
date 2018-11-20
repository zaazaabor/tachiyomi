/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.chapter

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetListOfObjects
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetObject
import com.pushtorefresh.storio3.sqlite.operations.put.PreparedPutCollectionOfObjects
import com.pushtorefresh.storio3.sqlite.queries.Query
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import tachiyomi.core.db.inTransaction
import tachiyomi.core.db.toRxOptional
import tachiyomi.core.db.withId
import tachiyomi.core.db.withIds
import tachiyomi.core.rx.RxOptional
import tachiyomi.data.chapter.resolver.ChapterSourceOrderPutResolver
import tachiyomi.data.chapter.table.ChapterTable
import tachiyomi.domain.chapter.interactor.SyncChaptersFromSource
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.repository.ChapterRepository
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

  override fun subscribeChapters(mangaId: Long): Flowable<List<Chapter>> {
    return preparedChapters(mangaId)
      .asRxFlowable(BackpressureStrategy.LATEST)
      .distinctUntilChanged() // TODO do we want to run a distinct?
  }

  override fun subscribeChapter(chapterId: Long): Flowable<RxOptional<Chapter>> {
    return preparedChapter(chapterId).asRxFlowable(BackpressureStrategy.LATEST)
      .distinctUntilChanged()
      .map { it.toRxOptional() }
  }

  override fun getChapters(mangaId: Long): Single<List<Chapter>> {
    return preparedChapters(mangaId).asRxSingle()
  }

  override fun getChapter(chapterId: Long): Maybe<Chapter> {
    return preparedChapter(chapterId).asRxMaybe()
  }

  override fun getChapter(chapterKey: String, mangaId: Long): Maybe<Chapter> {
    return storio.get()
      .`object`(Chapter::class.java)
      .withQuery(Query.builder()
        .table(ChapterTable.TABLE)
        .where("${ChapterTable.COL_KEY} = ? AND ${ChapterTable.COL_MANGA_ID} = ?")
        .whereArgs(chapterKey, mangaId)
        .build())
      .prepare()
      .asRxMaybe()
  }

  override fun saveChapters(chapters: List<Chapter>): Completable {
    return storio.put().`object`(chapters).prepare().asRxCompletable()
  }

  override fun deleteChapter(chapterId: Long): Completable {
    return storio.delete()
      .withId(ChapterTable.TABLE, ChapterTable.COL_ID, chapterId)
      .prepare()
      .asRxCompletable()
  }

  override fun deleteChapters(chapterIds: List<Long>): Completable {
    return storio.delete()
      .withIds(ChapterTable.TABLE, ChapterTable.COL_ID, chapterIds)
      .prepare()
      .asRxCompletable()
  }

  override fun syncChapters(
    diff: SyncChaptersFromSource.Diff,
    sourceChapters: List<Chapter>
  ): Completable {
    return Completable.fromAction {
      storio.inTransaction {
        val chaptersToSave = diff.added + diff.updated
        if (chaptersToSave.isNotEmpty()) {
          storio.put().objects(chaptersToSave).prepare().executeAsBlocking()
        }
        if (diff.deleted.isNotEmpty()) {
          storio.delete().objects(diff.deleted).prepare().executeAsBlocking()
        }

        fixChaptersSourceOrder(sourceChapters).executeAsBlocking()
      }
    }
  }

  override fun syncChapter(
    chapter: Chapter,
    sourceChapters: List<Chapter>
  ): Completable {
    return Completable.fromAction {
      storio.inTransaction {
        storio.put().`object`(chapter).prepare().executeAsBlocking()
        fixChaptersSourceOrder(sourceChapters).executeAsBlocking()
      }
    }
  }

  private fun fixChaptersSourceOrder(
    chapters: List<Chapter>
  ): PreparedPutCollectionOfObjects<Chapter> {
    return storio.put()
      .objects(chapters)
      .withPutResolver(ChapterSourceOrderPutResolver())
      .prepare()
  }

}
