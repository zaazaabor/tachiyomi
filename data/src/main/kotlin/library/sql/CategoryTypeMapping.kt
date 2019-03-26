/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.sql

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
import tachiyomi.data.library.sql.CategoryTable.COL_ID
import tachiyomi.data.library.sql.CategoryTable.COL_NAME
import tachiyomi.data.library.sql.CategoryTable.COL_ORDER
import tachiyomi.data.library.sql.CategoryTable.COL_UPDATE_INTERVAL
import tachiyomi.data.library.sql.CategoryTable.TABLE
import tachiyomi.domain.library.model.Category

internal class CategoryTypeMapping : SQLiteTypeMapping<Category>(
  CategoryPutResolver(),
  CategoryGetResolver(),
  CategoryDeleteResolver()
)

internal class CategoryPutResolver : DefaultPutResolver<Category>() {

  override fun mapToInsertQuery(obj: Category): InsertQuery {
    return InsertQuery.builder()
      .table(TABLE)
      .build()
  }

  override fun mapToUpdateQuery(obj: Category): UpdateQuery {
    return UpdateQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(obj.id)
      .build()
  }

  override fun mapToContentValues(obj: Category): ContentValues {
    return ContentValues(4).apply {
      put(COL_ID, obj.id.takeIf { it != -1L })
      put(COL_NAME, obj.name)
      put(COL_ORDER, obj.order)
      put(COL_UPDATE_INTERVAL, obj.updateInterval)
    }
  }
}

internal interface CategoryCursorMapper {

  fun mapCategory(cursor: Cursor): Category {
    val id = cursor.getLong(cursor.getColumnIndex(COL_ID))
    val name = cursor.getString(cursor.getColumnIndex(COL_NAME))
    val order = cursor.getInt(cursor.getColumnIndex(COL_ORDER))
    val flags = cursor.getInt(cursor.getColumnIndex(COL_UPDATE_INTERVAL))

    return Category(id, name, order, flags)
  }

}

internal open class CategoryGetResolver : DefaultGetResolver<Category>(),
  CategoryCursorMapper {

  override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): Category {
    return mapCategory(cursor)
  }
}

internal class CategoryDeleteResolver : DefaultDeleteResolver<Category>() {

  override fun mapToDeleteQuery(obj: Category): DeleteQuery {
    return DeleteQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(obj.id)
      .build()
  }
}
