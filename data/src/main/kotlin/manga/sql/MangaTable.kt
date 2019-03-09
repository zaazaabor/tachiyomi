/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.sql

import android.database.sqlite.SQLiteDatabase
import tachiyomi.core.db.DbOpenCallback

internal object MangaTable : DbOpenCallback {

  const val TABLE = "manga"

  const val LIBRARY = "library"

  const val COL_ID = "m_id"
  const val COL_SOURCE = "m_source"
  const val COL_KEY = "m_url"
  const val COL_TITLE = "m_title"
  const val COL_ARTIST = "m_artist"
  const val COL_AUTHOR = "m_author"
  const val COL_DESCRIPTION = "m_description"
  const val COL_GENRES = "m_genre"
  const val COL_STATUS = "m_status"
  const val COL_COVER = "m_cover"
  const val COL_FAVORITE = "m_favorite"
  const val COL_LAST_UPDATE = "m_last_update"
  const val COL_LAST_INIT = "m_last_init"
  const val COL_DATE_ADDED = "m_date_added"
  const val COL_VIEWER = "m_viewer"
  const val COL_FLAGS = "m_flags"

  private val createTableQuery: String
    get() = """CREATE TABLE $TABLE(
            $COL_ID INTEGER NOT NULL PRIMARY KEY,
            $COL_SOURCE INTEGER NOT NULL,
            $COL_KEY TEXT NOT NULL,
            $COL_TITLE TEXT NOT NULL,
            $COL_ARTIST TEXT,
            $COL_AUTHOR TEXT,
            $COL_DESCRIPTION TEXT,
            $COL_GENRES TEXT,
            $COL_STATUS INTEGER NOT NULL,
            $COL_COVER TEXT,
            $COL_FAVORITE INTEGER NOT NULL,
            $COL_LAST_UPDATE LONG,
            $COL_LAST_INIT LONG,
            $COL_DATE_ADDED LONG,
            $COL_VIEWER INTEGER NOT NULL,
            $COL_FLAGS INTEGER NOT NULL
            )"""

  val createUrlIndexQuery: String
    get() = "CREATE INDEX ${TABLE}_${COL_KEY}_index ON $TABLE($COL_KEY)"

  val createFavoriteIndexQuery: String
    get() = "CREATE INDEX ${TABLE}_${COL_FAVORITE}_index ON $TABLE($COL_FAVORITE)"

  val createFavoriteViewQuery: String
    get() = "CREATE VIEW $LIBRARY AS SELECT * FROM $TABLE WHERE $COL_FAVORITE = 1"

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(createTableQuery)
    db.execSQL(createFavoriteViewQuery)
    db.execSQL(createFavoriteIndexQuery)
    db.execSQL(createUrlIndexQuery)
  }

}
