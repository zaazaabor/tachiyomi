/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.sql

import android.database.sqlite.SQLiteDatabase
import tachiyomi.core.db.DbOpenCallback

internal object CatalogTable : DbOpenCallback {

  const val TABLE = "catalogs"

  const val COL_ID = "ct_id"
  const val COL_NAME = "ct_name"
  const val COL_PKGNAME = "ct_pkgname"
  const val COL_VCODE = "ct_vcode"
  const val COL_VNAME = "ct_vname"
  const val COL_LANG = "ct_lang"
  const val COL_APKURL = "ct_apkurl"
  const val COL_ICONURL = "ct_iconurl"

  private val createTableQuery: String
    get() = """CREATE TABLE $TABLE(
      $COL_ID INTEGER NOT NULL PRIMARY KEY,
      $COL_NAME TEXT NOT NULL,
      $COL_PKGNAME TEXT NOT NULL,
      $COL_VCODE INTEGER NOT NULL,
      $COL_VNAME TEXT,
      $COL_LANG TEXT,
      $COL_APKURL TEXT,
      $COL_ICONURL TEXT
    )"""

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(createTableQuery)
  }

}
