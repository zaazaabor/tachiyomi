/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.category.resolver

import android.content.ContentValues
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.put.PutResolver
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import tachiyomi.data.category.table.CategoryTable
import tachiyomi.domain.category.Category

internal abstract class CategoryUpdateResolver : PutResolver<Category>() {

  override fun performPut(storIOSQLite: StorIOSQLite, category: Category): PutResult {
    val query = UpdateQuery.builder()
      .table(CategoryTable.TABLE)
      .where("${CategoryTable.COL_ID} = ?")
      .whereArgs(category.id)
      .build()
    val numberOfRowsUpdated = storIOSQLite.lowLevel().update(query, mapToContentValues(category))
    return PutResult.newUpdateResult(numberOfRowsUpdated, query.table())
  }

  abstract fun mapToContentValues(category: Category): ContentValues
}
