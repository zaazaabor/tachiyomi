/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.chapter.sql

import android.content.ContentValues
import android.database.Cursor
import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import tachiyomi.data.chapter.sql.ChapterTable.COL_BOOKMARK
import tachiyomi.data.chapter.sql.ChapterTable.COL_DATE_FETCH
import tachiyomi.data.chapter.sql.ChapterTable.COL_DATE_UPLOAD
import tachiyomi.data.chapter.sql.ChapterTable.COL_ID
import tachiyomi.data.chapter.sql.ChapterTable.COL_KEY
import tachiyomi.data.chapter.sql.ChapterTable.COL_MANGA_ID
import tachiyomi.data.chapter.sql.ChapterTable.COL_NAME
import tachiyomi.data.chapter.sql.ChapterTable.COL_NUMBER
import tachiyomi.data.chapter.sql.ChapterTable.COL_PROGRESS
import tachiyomi.data.chapter.sql.ChapterTable.COL_READ
import tachiyomi.data.chapter.sql.ChapterTable.COL_SCANLATOR
import tachiyomi.data.chapter.sql.ChapterTable.COL_SOURCE_ORDER
import tachiyomi.data.chapter.sql.ChapterTable.TABLE
import tachiyomi.domain.chapter.model.Chapter

internal class ChapterTypeMapping : SQLiteTypeMapping<Chapter>(
  ChapterPutResolver(),
  ChapterGetResolver(),
  ChapterDeleteResolver()
)

internal class ChapterPutResolver : DefaultPutResolver<Chapter>() {

  override fun mapToInsertQuery(obj: Chapter): InsertQuery {
    return InsertQuery.builder()
      .table(TABLE)
      .build()
  }

  override fun mapToUpdateQuery(obj: Chapter): UpdateQuery {
    return UpdateQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(obj.id)
      .build()
  }

  override fun mapToContentValues(obj: Chapter): ContentValues {
    return ContentValues(12).apply {
      put(COL_ID, obj.id.takeIf { it != -1L })
      put(COL_MANGA_ID, obj.mangaId)
      put(COL_KEY, obj.key)
      put(COL_NAME, obj.name)
      put(COL_READ, obj.read)
      put(COL_SCANLATOR, obj.scanlator)
      put(COL_BOOKMARK, obj.bookmark)
      put(COL_DATE_FETCH, obj.dateFetch)
      put(COL_DATE_UPLOAD, obj.dateUpload)
      put(COL_PROGRESS, obj.progress)
      put(COL_NUMBER, obj.number)
      put(COL_SOURCE_ORDER, obj.sourceOrder)
    }
  }
}

internal class ChapterGetResolver : DefaultGetResolver<Chapter>() {
  override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): Chapter {
    return Chapter(
      id = cursor.getLong(cursor.getColumnIndex(COL_ID)),
      mangaId = cursor.getLong(cursor.getColumnIndex(COL_MANGA_ID)),
      key = cursor.getString(cursor.getColumnIndex(COL_KEY)),
      name = cursor.getString(cursor.getColumnIndex(COL_NAME)),
      scanlator = cursor.getString(cursor.getColumnIndex(COL_SCANLATOR)),
      read = cursor.getInt(cursor.getColumnIndex(COL_READ)) == 1,
      bookmark = cursor.getInt(cursor.getColumnIndex(COL_BOOKMARK)) == 1,
      dateFetch = cursor.getLong(cursor.getColumnIndex(COL_DATE_FETCH)),
      dateUpload = cursor.getLong(cursor.getColumnIndex(COL_DATE_UPLOAD)),
      progress = cursor.getInt(cursor.getColumnIndex(COL_PROGRESS)),
      number = cursor.getFloat(cursor.getColumnIndex(COL_NUMBER)),
      sourceOrder = cursor.getInt(cursor.getColumnIndex(COL_SOURCE_ORDER))
    )
  }
}

internal class ChapterDeleteResolver : DefaultDeleteResolver<Chapter>() {

  override fun mapToDeleteQuery(obj: Chapter): DeleteQuery {
    return DeleteQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(obj.id)
      .build()
  }
}
