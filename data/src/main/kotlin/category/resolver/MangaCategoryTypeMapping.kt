/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.category.resolver

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
import tachiyomi.data.category.table.MangaCategoryTable
import tachiyomi.data.category.table.MangaCategoryTable.COL_CATEGORY_ID
import tachiyomi.data.category.table.MangaCategoryTable.COL_MANGA_ID
import tachiyomi.data.category.table.MangaCategoryTable.TABLE
import tachiyomi.domain.category.model.MangaCategory

internal class MangaCategoryTypeMapping : SQLiteTypeMapping<MangaCategory>(
  MangaCategoryPutResolver(),
  MangaCategoryGetResolver(),
  MangaCategoryDeleteResolver()
)

internal class MangaCategoryPutResolver : DefaultPutResolver<MangaCategory>() {

  override fun mapToInsertQuery(obj: MangaCategory): InsertQuery {
    return InsertQuery.builder()
      .table(TABLE)
      .build()
  }

  override fun mapToUpdateQuery(obj: MangaCategory): UpdateQuery {
    return UpdateQuery.builder()
      .table(TABLE)
      .where("$COL_MANGA_ID = ? AND $COL_CATEGORY_ID = ?")
      .whereArgs(obj.mangaId, obj.categoryId)
      .build()
  }

  override fun mapToContentValues(obj: MangaCategory): ContentValues {
    return ContentValues(2).apply {
      put(COL_MANGA_ID, obj.mangaId)
      put(COL_CATEGORY_ID, obj.categoryId)
    }
  }
}

internal class MangaCategoryGetResolver : DefaultGetResolver<MangaCategory>() {

  override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): MangaCategory {
    val mangaId = cursor.getLong(cursor.getColumnIndex(COL_MANGA_ID))
    val categoryId = cursor.getLong(cursor.getColumnIndex(COL_CATEGORY_ID))

    return MangaCategory(mangaId, categoryId)
  }
}

internal class MangaCategoryDeleteResolver : DefaultDeleteResolver<MangaCategory>() {

  override fun mapToDeleteQuery(obj: MangaCategory): DeleteQuery {
    return DeleteQuery.builder()
      .table(MangaCategoryTable.TABLE)
      .where("$COL_MANGA_ID = ? AND $COL_CATEGORY_ID = ?")
      .whereArgs(obj.mangaId, obj.categoryId)
      .build()
  }
}
