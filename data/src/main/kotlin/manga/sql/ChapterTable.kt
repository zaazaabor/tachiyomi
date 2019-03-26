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

internal object ChapterTable : DbOpenCallback {

  const val TABLE = "chapters"

  const val COL_ID = "c_id"
  const val COL_MANGA_ID = "c_manga_id"
  const val COL_KEY = "c_url"
  const val COL_NAME = "c_name"
  const val COL_READ = "c_read"
  const val COL_SCANLATOR = "c_scanlator"
  const val COL_BOOKMARK = "c_bookmark"
  const val COL_DATE_FETCH = "c_date_fetch"
  const val COL_DATE_UPLOAD = "c_date_upload"
  const val COL_PROGRESS = "c_last_page_read"
  const val COL_NUMBER = "c_number"
  const val COL_SOURCE_ORDER = "source_order"

  private val createTableQuery: String
    get() = """CREATE TABLE $TABLE(
            $COL_ID INTEGER NOT NULL PRIMARY KEY,
            $COL_MANGA_ID INTEGER NOT NULL,
            $COL_KEY TEXT NOT NULL,
            $COL_NAME TEXT NOT NULL,
            $COL_SCANLATOR TEXT,
            $COL_READ BOOLEAN NOT NULL,
            $COL_BOOKMARK BOOLEAN NOT NULL,
            $COL_PROGRESS INT NOT NULL,
            $COL_NUMBER FLOAT NOT NULL,
            $COL_SOURCE_ORDER INTEGER NOT NULL,
            $COL_DATE_FETCH LONG NOT NULL,
            $COL_DATE_UPLOAD LONG NOT NULL,
            FOREIGN KEY($COL_MANGA_ID) REFERENCES ${MangaTable.TABLE} (${MangaTable.COL_ID})
            ON DELETE CASCADE
            )"""

  val createMangaIdIndex
    get() = "CREATE INDEX ${TABLE}_${COL_ID}_index ON $TABLE($COL_MANGA_ID)"

  val createUnreadIndex
    get() = "CREATE INDEX ${TABLE}_unread_index ON $TABLE($COL_MANGA_ID, $COL_READ) WHERE " +
      "$COL_READ = 0"

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(createTableQuery)
    db.execSQL(createMangaIdIndex)
    db.execSQL(createUnreadIndex)
  }

}
