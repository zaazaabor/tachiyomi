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
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.data.manga.sql.ChapterTable as Chapter
import tachiyomi.data.manga.sql.MangaTable as Manga

internal object LibraryMangaGetResolver : DefaultGetResolver<LibraryManga>() {

  private const val selections = """${Manga.COL_ID}, ${Manga.COL_SOURCE}, ${Manga.COL_KEY},
    ${Manga.COL_TITLE}, ${Manga.COL_STATUS}, ${Manga.COL_COVER}, ${Manga.COL_LAST_UPDATE}"""

  private const val allQuery = """SELECT $selections, COUNT(${Chapter.COL_ID}) AS unread
    FROM ${Manga.LIBRARY}
    LEFT JOIN ${Chapter.TABLE}
      ON ${Manga.COL_ID} = ${Chapter.COL_MANGA_ID} AND ${Chapter.COL_READ} = 0
    GROUP BY ${Manga.COL_ID}"""

  private const val uncatQuery = """SELECT $selections, COUNT(${Chapter.COL_ID}) AS unread
    FROM ${Manga.LIBRARY}
    LEFT JOIN ${Chapter.TABLE}
      ON ${Manga.COL_ID} = ${Chapter.COL_MANGA_ID} AND ${Chapter.COL_READ} = 0
    WHERE NOT EXISTS (
      SELECT ${MangaCategoryTable.COL_MANGA_ID}
      FROM ${MangaCategoryTable.TABLE}
      WHERE ${Manga.COL_ID} = ${MangaCategoryTable.COL_MANGA_ID}
    )
    GROUP BY ${Manga.COL_ID}"""

  private const val categoryQuery = """SELECT $selections, COUNT(${Chapter.COL_ID}) AS unread
    FROM ${Manga.LIBRARY}
    JOIN ${MangaCategoryTable.TABLE}
      ON ${Manga.COL_ID} = ${MangaCategoryTable.COL_MANGA_ID}
      AND ${MangaCategoryTable.COL_CATEGORY_ID} = ?1
    LEFT JOIN ${Chapter.TABLE}
      ON ${Manga.COL_ID} = ${Chapter.COL_MANGA_ID} AND ${Chapter.COL_READ} = 0
    GROUP BY ${Manga.COL_ID}"""

  private const val allQueryWithTotalChapters = """SELECT $selections,
      COUNT(${Chapter.COL_ID}) AS total,
      SUM(CASE WHEN ${Chapter.COL_READ} == 0 THEN 1 ELSE 0 END) AS unread
    FROM ${Manga.LIBRARY}
    LEFT JOIN ${Chapter.TABLE}
    ON ${Manga.COL_ID} = ${Chapter.COL_MANGA_ID}
    GROUP BY ${Manga.COL_ID}"""

  private const val uncatQueryWithTotalChapters = """SELECT $selections,
      COUNT(${Chapter.COL_ID}) AS total,
      SUM(CASE WHEN ${Chapter.COL_READ} == 0 THEN 1 ELSE 0 END) AS unread
    FROM ${Manga.LIBRARY}
    LEFT JOIN ${Chapter.TABLE}
      ON ${Manga.COL_ID} = ${Chapter.COL_MANGA_ID}
    WHERE NOT EXISTS (
      SELECT ${MangaCategoryTable.COL_MANGA_ID}
      FROM ${MangaCategoryTable.TABLE}
      WHERE ${Manga.COL_ID} = ${MangaCategoryTable.COL_MANGA_ID}
    )
    GROUP BY ${Manga.COL_ID}"""

  private const val categoryQueryWithTotalChapters = """SELECT $selections,
      COUNT(${Chapter.COL_ID}) AS total,
      SUM(CASE WHEN ${Chapter.COL_READ} == 0 THEN 1 ELSE 0 END) AS unread
    FROM ${Manga.LIBRARY}
    JOIN ${MangaCategoryTable.TABLE}
      ON ${Manga.COL_ID} = ${MangaCategoryTable.COL_MANGA_ID}
      AND ${MangaCategoryTable.COL_CATEGORY_ID} = ?1
    LEFT JOIN ${Chapter.TABLE}
      ON ${Manga.COL_ID} = ${Chapter.COL_MANGA_ID}
    GROUP BY ${Manga.COL_ID}"""

  fun getAllQuery(sort: LibrarySort) = when (sort) {
    is LibrarySort.Title -> "$allQuery ORDER BY ${Manga.COL_TITLE} ${sort.dir}"
    is LibrarySort.LastRead -> allQuery // TODO implement history
    is LibrarySort.LastUpdated -> "$allQuery ORDER BY ${Manga.COL_LAST_UPDATE} ${sort.dir}"
    is LibrarySort.Unread -> "$allQuery ORDER BY unread ${sort.dir}"
    is LibrarySort.TotalChapters -> "$allQueryWithTotalChapters ORDER BY total ${sort.dir}"
    is LibrarySort.Source -> "$allQuery ORDER BY ${Manga.COL_SOURCE} ${sort.dir}, " +
      "${Manga.COL_TITLE} ${sort.dir}"
  }

  fun getUncategorizedQuery(sort: LibrarySort) = when (sort) {
    is LibrarySort.Title -> "$uncatQuery ORDER BY ${Manga.COL_TITLE} ${sort.dir}"
    is LibrarySort.LastRead -> uncatQuery // TODO implement history
    is LibrarySort.LastUpdated -> "$uncatQuery ORDER BY ${Manga.COL_LAST_UPDATE} ${sort.dir}"
    is LibrarySort.Unread -> "$uncatQuery ORDER BY unread ${sort.dir}"
    is LibrarySort.TotalChapters -> "$uncatQueryWithTotalChapters ORDER BY total ${sort.dir}"
    is LibrarySort.Source -> "$uncatQuery ORDER BY ${Manga.COL_SOURCE} ${sort.dir}, " +
      "${Manga.COL_TITLE} ${sort.dir}"
  }

  fun getCategoryQuery(sort: LibrarySort) = when (sort) {
    is LibrarySort.Title -> "$categoryQuery ORDER BY ${Manga.COL_TITLE} ${sort.dir}"
    is LibrarySort.LastRead -> categoryQuery // TODO implement history
    is LibrarySort.LastUpdated -> "$categoryQuery ORDER BY ${Manga.COL_LAST_UPDATE} ${sort.dir}"
    is LibrarySort.Unread -> "$categoryQuery ORDER BY unread ${sort.dir}"
    is LibrarySort.TotalChapters -> "$categoryQueryWithTotalChapters ORDER BY total ${sort.dir}"
    is LibrarySort.Source -> "$categoryQuery ORDER BY ${Manga.COL_SOURCE} ${sort.dir}, " +
      "${Manga.COL_TITLE} ${sort.dir}"
  }

  private val LibrarySort.dir get() = if (ascending) "ASC" else "DESC"

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
