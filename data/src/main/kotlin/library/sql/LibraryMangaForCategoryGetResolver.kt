/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.sql

import android.database.Cursor
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.data.category.sql.MangaCategoryTable as MangaCategory
import tachiyomi.data.chapter.sql.ChapterTable as Chapter
import tachiyomi.data.manga.sql.MangaTable as Manga

internal object LibraryMangaForCategoryGetResolver : DefaultGetResolver<LibraryManga>() {

  private const val mangaSelections = """${Manga.COL_ID}, ${Manga.COL_SOURCE}, ${Manga.COL_KEY},
    ${Manga.COL_TITLE}, ${Manga.COL_STATUS}, ${Manga.COL_COVER}, ${Manga.COL_LAST_UPDATE}"""

  /**
   * Query to get the manga from the library for a given category.
   */
  const val query = """SELECT $mangaSelections, COUNT(${Chapter.COL_ID}) as unread
    FROM ${Manga.LIBRARY}
    JOIN ${MangaCategory.TABLE}
    ON ${Manga.COL_ID} = ${MangaCategory.COL_MANGA_ID}
      AND ${MangaCategory.COL_CATEGORY_ID} = ?1
    LEFT JOIN ${Chapter.TABLE}
    ON ${Manga.COL_ID} = ${Chapter.COL_MANGA_ID} AND ${Chapter.COL_READ} = 0
    GROUP BY ${Manga.COL_ID}"""

  override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): LibraryManga {
    val id = cursor.getLong(cursor.getColumnIndex(Manga.COL_ID))
    val source = cursor.getLong(cursor.getColumnIndex(Manga.COL_SOURCE))
    val key = cursor.getString(cursor.getColumnIndex(Manga.COL_KEY))
    val title = cursor.getString(cursor.getColumnIndex(Manga.COL_TITLE))
    val status = cursor.getInt(cursor.getColumnIndex(Manga.COL_STATUS))
    val cover = cursor.getString(cursor.getColumnIndex(Manga.COL_COVER))
    val lastUpdate = cursor.getLong(cursor.getColumnIndex(Manga.COL_LAST_UPDATE))
    val unread = cursor.getInt(cursor.getColumnIndex("unread"))

    return LibraryManga(id, source, key, title, status, cover, lastUpdate, unread)
  }

}
