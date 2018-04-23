package tachiyomi.core.db

import com.pushtorefresh.storio3.Optional
import com.pushtorefresh.storio3.Queries
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.delete.PreparedDelete
import com.pushtorefresh.storio3.sqlite.operations.delete.PreparedDeleteByQuery
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import tachiyomi.core.rx.RxOptional

inline fun StorIOSQLite.inTransaction(block: () -> Unit) {
  lowLevel().beginTransaction()
  try {
    block()
    lowLevel().setTransactionSuccessful()
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
  ids: List<Long>
): PreparedDeleteByQuery.Builder {
  return byQuery(DeleteQuery.builder()
    .table(table)
    .where("$columnName IN (${Queries.placeholders(ids.size)})")
    .whereArgs(*ids.toTypedArray())
    .build()
  )
}

fun <T> Optional<T>.toRxOptional(): RxOptional<T> {
  return RxOptional.of(orNull())
}
