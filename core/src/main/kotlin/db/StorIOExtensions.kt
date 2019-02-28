/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.db

import com.pushtorefresh.storio3.Optional
import com.pushtorefresh.storio3.Queries
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.delete.PreparedDelete
import com.pushtorefresh.storio3.sqlite.operations.delete.PreparedDeleteByQuery
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery

inline fun StorIOSQLite.inTransaction(block: () -> Unit) {
  lowLevel().beginTransaction()
  try {
    block()
    lowLevel().setTransactionSuccessful()
  } finally {
    lowLevel().endTransaction()
  }
}

inline fun <T> StorIOSQLite.inTransactionReturn(block: () -> T): T {
  lowLevel().beginTransaction()
  try {
    val result = block()
    lowLevel().setTransactionSuccessful()
    return result
  } finally {
    lowLevel().endTransaction()
  }
}

fun PreparedDelete.Builder.withId(
  table: String,
  columnName: String,
  id: Long
): PreparedDeleteByQuery.Builder {
  return byQuery(DeleteQuery.builder()
    .table(table)
    .where("$columnName = ?")
    .whereArgs(id)
    .build()
  )
}

fun PreparedDelete.Builder.withIds(
  table: String,
  columnName: String,
  ids: Collection<Long>
): PreparedDeleteByQuery.Builder {
  return byQuery(DeleteQuery.builder()
    .table(table)
    .where("$columnName IN (${Queries.placeholders(ids.size)})")
    .whereArgs(*ids.toTypedArray())
    .build()
  )
}

fun <T> Optional<T>.toOptional(): tachiyomi.core.stdlib.Optional<T> {
  return tachiyomi.core.stdlib.Optional.of(orNull())
}
