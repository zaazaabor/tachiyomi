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
import tachiyomi.domain.category.Category

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

  private val createAllCategoryQuery: String
    get() = """INSERT INTO $TABLE VALUES (${Category.ALL_ID}, "", 0, 0)"""

  private val createUncategorizedCategoryQuery: String
    get() = """INSERT INTO $TABLE VALUES (${Category.UNCATEGORIZED_ID}, "", 0, 0)"""

  private val deleteCategoryTrigger: String
    get() = """CREATE TRIGGER system_categories_deletion_trigger
      BEFORE DELETE
      ON $TABLE
      BEGIN
      SELECT CASE
      WHEN OLD.$COL_ID <= 0
      THEN RAISE(ABORT, 'System category cant be deleted')
      END;
      END;"""

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(createTableQuery)
    db.execSQL(deleteCategoryTrigger)
    db.execSQL(createAllCategoryQuery)
    db.execSQL(createUncategorizedCategoryQuery)
  }

}
