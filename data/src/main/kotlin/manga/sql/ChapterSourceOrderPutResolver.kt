/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.sql

import android.content.ContentValues
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.put.PutResolver
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import tachiyomi.core.db.inTransactionReturn
import tachiyomi.domain.manga.model.Chapter

object ChapterSourceOrderPutResolver : PutResolver<Chapter>() {

  override fun performPut(db: StorIOSQLite, chapter: Chapter) = db.inTransactionReturn {
    val updateQuery = mapToUpdateQuery(chapter)
    val contentValues = mapToContentValues(chapter)

    val numberOfRowsUpdated = db.lowLevel().update(updateQuery, contentValues)
    PutResult.newUpdateResult(numberOfRowsUpdated, updateQuery.table())
  }

  fun mapToUpdateQuery(chapter: Chapter) = UpdateQuery.builder()
    .table(ChapterTable.TABLE)
    .where("${ChapterTable.COL_KEY} = ? AND ${ChapterTable.COL_MANGA_ID} = ?")
    .whereArgs(chapter.key, chapter.mangaId)
    .build()

  fun mapToContentValues(chapter: Chapter) = ContentValues(1).apply {
    put(ChapterTable.COL_SOURCE_ORDER, chapter.sourceOrder)
  }

}
