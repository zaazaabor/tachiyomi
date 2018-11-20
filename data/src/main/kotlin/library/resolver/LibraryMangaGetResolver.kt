/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.resolver

import android.database.Cursor
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver
import tachiyomi.data.category.table.MangaCategoryTable
import tachiyomi.data.chapter.table.ChapterTable
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.domain.library.model.LibraryManga

internal object LibraryMangaGetResolver : DefaultGetResolver<LibraryManga>() {

  /**
   * Query to get the manga from the library, with their categories and unread count.
   */
  val query = """
    SELECT M.*, COALESCE(MC.${MangaCategoryTable.COL_CATEGORY_ID}, 0) AS a_category
    FROM (
        SELECT ${MangaTable.TABLE}.*, COALESCE(C.unread, 0) AS a_unread
        FROM ${MangaTable.TABLE}
        LEFT JOIN (
            SELECT ${ChapterTable.COL_MANGA_ID}, COUNT(*) AS unread
            FROM ${ChapterTable.TABLE}
            WHERE ${ChapterTable.COL_READ} = 0
            GROUP BY ${ChapterTable.COL_MANGA_ID}
        ) AS C
        ON ${MangaTable.COL_ID} = C.${ChapterTable.COL_MANGA_ID}
        WHERE ${MangaTable.COL_FAVORITE} = 1
        GROUP BY ${MangaTable.COL_ID}
        ORDER BY ${MangaTable.COL_TITLE}
    ) AS M
    LEFT JOIN (
        SELECT * FROM ${MangaCategoryTable.TABLE}) AS MC
        ON MC.${MangaCategoryTable.COL_MANGA_ID} = M.${MangaTable.COL_ID}
  """

  override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): LibraryManga {
    val id = cursor.getLong(cursor.getColumnIndex(MangaTable.COL_ID))
    val source = cursor.getLong(cursor.getColumnIndex(MangaTable.COL_SOURCE))
    val key = cursor.getString(cursor.getColumnIndex(MangaTable.COL_KEY))
    val title = cursor.getString(cursor.getColumnIndex(MangaTable.COL_TITLE))
    val status = cursor.getInt(cursor.getColumnIndex(MangaTable.COL_STATUS))
    val cover = cursor.getString(cursor.getColumnIndex(MangaTable.COL_COVER))
    val lastUpdate = cursor.getLong(cursor.getColumnIndex(MangaTable.COL_LAST_UPDATE))
    val category = cursor.getLong(cursor.getColumnIndex("a_category"))
    val unread = cursor.getInt(cursor.getColumnIndex("a_unread"))

    return LibraryManga(id, source, key, title, status, cover, lastUpdate, category, unread)
  }

}
