/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.category.resolver

import android.database.Cursor
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver
import tachiyomi.data.category.table.CategoryTable
import tachiyomi.data.category.table.MangaCategoryTable
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.domain.category.Category
import tachiyomi.domain.category.CategoryWithCount

internal object CategoryWithCountGetResolver : DefaultGetResolver<CategoryWithCount>(),
  CategoryCursorMapper {

  private const val mangaCount = "manga_count"

  const val query = """
    SELECT ${CategoryTable.TABLE}.*, COUNT(${MangaCategoryTable.COL_MANGA_ID}) as $mangaCount
    FROM ${CategoryTable.TABLE}
    LEFT JOIN ${MangaCategoryTable.TABLE}
    ON ${CategoryTable.COL_ID} = ${MangaCategoryTable.COL_CATEGORY_ID}
    WHERE ${CategoryTable.COL_ID} != ${Category.ALL_ID}
    GROUP BY ${CategoryTable.COL_ID}
    UNION
    SELECT *, (SELECT COUNT() FROM ${MangaTable.LIBRARY})
    FROM ${CategoryTable.TABLE}
    WHERE ${CategoryTable.COL_ID} = ${Category.ALL_ID}
    ORDER BY ${CategoryTable.COL_ORDER}
  """

  override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): CategoryWithCount {
    val category = mapCategory(cursor)
    val count = cursor.getInt(cursor.getColumnIndex(mangaCount))
    return CategoryWithCount(category, count)
  }

}
