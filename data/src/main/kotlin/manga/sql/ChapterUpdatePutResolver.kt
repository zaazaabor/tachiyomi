/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.sql

import android.content.ContentValues
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import tachiyomi.core.util.optBoolean
import tachiyomi.core.util.optFloat
import tachiyomi.core.util.optInt
import tachiyomi.core.util.optLong
import tachiyomi.core.util.optString
import tachiyomi.data.manga.sql.ChapterTable.COL_BOOKMARK
import tachiyomi.data.manga.sql.ChapterTable.COL_DATE_FETCH
import tachiyomi.data.manga.sql.ChapterTable.COL_DATE_UPLOAD
import tachiyomi.data.manga.sql.ChapterTable.COL_ID
import tachiyomi.data.manga.sql.ChapterTable.COL_KEY
import tachiyomi.data.manga.sql.ChapterTable.COL_MANGA_ID
import tachiyomi.data.manga.sql.ChapterTable.COL_NAME
import tachiyomi.data.manga.sql.ChapterTable.COL_NUMBER
import tachiyomi.data.manga.sql.ChapterTable.COL_PROGRESS
import tachiyomi.data.manga.sql.ChapterTable.COL_READ
import tachiyomi.data.manga.sql.ChapterTable.COL_SCANLATOR
import tachiyomi.data.manga.sql.ChapterTable.COL_SOURCE_ORDER
import tachiyomi.data.manga.sql.ChapterTable.TABLE
import tachiyomi.domain.manga.model.ChapterUpdate

object ChapterUpdatePutResolver : DefaultPutResolver<ChapterUpdate>() {

  override fun mapToInsertQuery(update: ChapterUpdate): InsertQuery {
    throw IllegalStateException("Partial update can't perform insert of chapter ${update.id}")
  }

  override fun mapToUpdateQuery(update: ChapterUpdate): UpdateQuery {
    return UpdateQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(update.id)
      .build()
  }

  override fun mapToContentValues(update: ChapterUpdate): ContentValues {
    return ContentValues(12).apply {
      put(COL_ID, update.id)
      optLong(COL_MANGA_ID, update.mangaId)
      optString(COL_KEY, update.key)
      optString(COL_NAME, update.name)
      optBoolean(COL_READ, update.read)
      optBoolean(COL_BOOKMARK, update.bookmark)
      optInt(COL_PROGRESS, update.progress)
      optLong(COL_DATE_UPLOAD, update.dateUpload)
      optLong(COL_DATE_FETCH, update.dateFetch)
      optInt(COL_SOURCE_ORDER, update.sourceOrder)
      optFloat(COL_NUMBER, update.number)
      optString(COL_SCANLATOR, update.scanlator)
    }
  }

}
