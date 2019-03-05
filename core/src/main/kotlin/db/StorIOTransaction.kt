/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.db

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import javax.inject.Inject

class StorIOTransaction @Inject internal constructor(storio: StorIOSQLite) : Transaction {

  private val lowLevel = storio.lowLevel()

  override fun begin() {
    lowLevel.beginTransaction()
  }

  override fun commit() {
    lowLevel.setTransactionSuccessful()
  }

  override fun end() {
    lowLevel.endTransaction()
  }

}
