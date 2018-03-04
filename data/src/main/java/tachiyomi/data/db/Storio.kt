package tachiyomi.data.db

import android.database.sqlite.SQLiteOpenHelper
import com.pushtorefresh.storio3.Queries
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.delete.PreparedDelete
import com.pushtorefresh.storio3.sqlite.operations.delete.PreparedDeleteByQuery
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import tachiyomi.data.category.resolver.CategoryTypeMapping
import tachiyomi.data.chapter.model.Chapter
import tachiyomi.data.chapter.resolver.ChapterTypeMapping
import tachiyomi.data.manga.resolver.MangaTypeMapping
import tachiyomi.domain.category.Category
import tachiyomi.domain.manga.Manga

fun SQLiteOpenHelper.wrapDb(): StorIOSQLite {
  return DefaultStorIOSQLite.builder()
    .sqliteOpenHelper(this)
    .addTypeMapping(Manga::class.java, MangaTypeMapping())
    .addTypeMapping(Chapter::class.java, ChapterTypeMapping())
    .addTypeMapping(Category::class.java, CategoryTypeMapping())
    .build()
}

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
