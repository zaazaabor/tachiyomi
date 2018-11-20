/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.category.table

import android.database.sqlite.SQLiteDatabase
import tachiyomi.core.db.DbOpenCallback

internal object CategoryTable : DbOpenCallback {

  const val TABLE = "categories"

  const val COL_ID = "ca_id"
  const val COL_NAME = "ca_name"
  const val COL_ORDER = "ca_sort"
  const val COL_FLAGS = "ca_flags"

  private val createTableQuery: String
    get() = """CREATE TABLE $TABLE(
            $COL_ID INTEGER NOT NULL PRIMARY KEY,
            $COL_NAME TEXT NOT NULL,
            $COL_ORDER INTEGER NOT NULL,
            $COL_FLAGS INTEGER NOT NULL
            )"""

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(createTableQuery)
  }

}
