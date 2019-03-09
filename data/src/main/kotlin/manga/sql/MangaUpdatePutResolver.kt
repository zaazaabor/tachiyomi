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
import tachiyomi.core.stdlib.Optional
import tachiyomi.core.util.optBoolean
import tachiyomi.core.util.optInt
import tachiyomi.core.util.optLong
import tachiyomi.core.util.optString
import tachiyomi.data.manga.sql.MangaTable.COL_ARTIST
import tachiyomi.data.manga.sql.MangaTable.COL_AUTHOR
import tachiyomi.data.manga.sql.MangaTable.COL_COVER
import tachiyomi.data.manga.sql.MangaTable.COL_DATE_ADDED
import tachiyomi.data.manga.sql.MangaTable.COL_DESCRIPTION
import tachiyomi.data.manga.sql.MangaTable.COL_FAVORITE
import tachiyomi.data.manga.sql.MangaTable.COL_FLAGS
import tachiyomi.data.manga.sql.MangaTable.COL_GENRES
import tachiyomi.data.manga.sql.MangaTable.COL_ID
import tachiyomi.data.manga.sql.MangaTable.COL_KEY
import tachiyomi.data.manga.sql.MangaTable.COL_LAST_INIT
import tachiyomi.data.manga.sql.MangaTable.COL_LAST_UPDATE
import tachiyomi.data.manga.sql.MangaTable.COL_SOURCE
import tachiyomi.data.manga.sql.MangaTable.COL_STATUS
import tachiyomi.data.manga.sql.MangaTable.COL_TITLE
import tachiyomi.data.manga.sql.MangaTable.COL_VIEWER
import tachiyomi.data.manga.sql.MangaTable.TABLE
import tachiyomi.domain.manga.model.MangaUpdate

object MangaUpdatePutResolver : DefaultPutResolver<MangaUpdate>() {

  override fun mapToInsertQuery(update: MangaUpdate): InsertQuery {
    throw IllegalStateException("Partial update can't perform insert on manga ${update.id}")
  }

  override fun mapToUpdateQuery(update: MangaUpdate): UpdateQuery {
    return UpdateQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(update.id)
      .build()
  }

  override fun mapToContentValues(update: MangaUpdate): ContentValues {
    return ContentValues().apply {
      put(COL_ID, update.id)
      optLong(COL_SOURCE, update.source)
      optString(COL_KEY, update.key)
      optString(COL_TITLE, update.title)
      optString(COL_ARTIST, update.artist)
      optString(COL_AUTHOR, update.author)
      optString(COL_DESCRIPTION, update.description)
      update.genres.let {
        if (it is Optional.Some) put(COL_GENRES, it.value.joinToString(separator = ";"))
      }
      optInt(COL_STATUS, update.status)
      optString(COL_COVER, update.cover)
      optBoolean(COL_FAVORITE, update.favorite)
      optLong(COL_LAST_UPDATE, update.lastUpdate)
      optLong(COL_LAST_INIT, update.lastInit)
      optLong(COL_DATE_ADDED, update.dateAdded)
      optInt(COL_VIEWER, update.viewer)
      optInt(COL_FLAGS, update.flags)
    }
  }

}
