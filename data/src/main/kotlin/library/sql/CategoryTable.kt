/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.sql

import android.database.sqlite.SQLiteDatabase
import tachiyomi.core.db.DbOpenCallback
import tachiyomi.domain.library.model.Category

internal object CategoryTable : DbOpenCallback {

  const val TABLE = "categories"

  const val COL_ID = "ca_id"
  const val COL_NAME = "ca_name"
  const val COL_ORDER = "ca_order"
  const val COL_UPDATE_INTERVAL = "ca_update_interval"
  const val COL_USE_OWN_FILTERS = "ca_use_own_filters"
  const val COL_FILTERS = "col_filters"
  const val COL_SORTING = "col_sorting"

  private val createTableQuery: String
    get() = """CREATE TABLE $TABLE(
      $COL_ID INTEGER NOT NULL PRIMARY KEY,
      $COL_NAME TEXT NOT NULL,
      $COL_ORDER INTEGER NOT NULL,
      $COL_UPDATE_INTERVAL INTEGER NOT NULL,
      $COL_USE_OWN_FILTERS INTEGER NOT NULL,
      $COL_FILTERS TEXT,
      $COL_SORTING TEXT
      )"""

  private val createAllCategoryQuery: String
    get() = """INSERT INTO $TABLE VALUES (${Category.ALL_ID}, "", 0, 0, 0, "", "")"""

  private val createUncategorizedCategoryQuery: String
    get() = """INSERT INTO $TABLE VALUES (${Category.UNCATEGORIZED_ID}, "", 0, 0, 0, "", "")"""

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
