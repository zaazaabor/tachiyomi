/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.db

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * An interface to receive an event when the database is created or upgraded.
 */
interface DbOpenCallback {

  /**
   * Called when the [db] is created for the first time.
   */
  fun onCreate(db: SupportSQLiteDatabase)

  /**
   * Called when the [db] needs to be upgraded from [oldVersion] to [newVersion]. The implementation
   * of this method is optional.
   */
  fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
  }

}
