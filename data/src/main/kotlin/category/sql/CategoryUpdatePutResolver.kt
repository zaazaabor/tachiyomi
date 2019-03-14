/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.category.sql

import android.content.ContentValues
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import tachiyomi.core.util.optInt
import tachiyomi.core.util.optString
import tachiyomi.data.category.sql.CategoryTable.COL_ID
import tachiyomi.data.category.sql.CategoryTable.COL_NAME
import tachiyomi.data.category.sql.CategoryTable.COL_ORDER
import tachiyomi.data.category.sql.CategoryTable.COL_UPDATE_INTERVAL
import tachiyomi.data.category.sql.CategoryTable.TABLE
import tachiyomi.domain.category.model.CategoryUpdate

object CategoryUpdatePutResolver : DefaultPutResolver<CategoryUpdate>() {

  override fun mapToInsertQuery(update: CategoryUpdate): InsertQuery {
    throw IllegalStateException("Partial update can't perform insert on category ${update.id}")
  }

  override fun mapToUpdateQuery(update: CategoryUpdate): UpdateQuery {
    return UpdateQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(update.id)
      .build()
  }

  override fun mapToContentValues(update: CategoryUpdate): ContentValues {
    return ContentValues(4).apply {
      put(COL_ID, update.id)
      optString(COL_NAME, update.name)
      optInt(COL_ORDER, update.order)
      optInt(COL_UPDATE_INTERVAL, update.updateInterval)
    }
  }

}
