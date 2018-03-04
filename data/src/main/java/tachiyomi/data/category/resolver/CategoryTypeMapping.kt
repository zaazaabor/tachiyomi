package tachiyomi.data.category.resolver

import android.content.ContentValues
import android.database.Cursor
import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import tachiyomi.data.category.table.CategoryTable.COL_FLAGS
import tachiyomi.data.category.table.CategoryTable.COL_ID
import tachiyomi.data.category.table.CategoryTable.COL_NAME
import tachiyomi.data.category.table.CategoryTable.COL_ORDER
import tachiyomi.data.category.table.CategoryTable.TABLE
import tachiyomi.domain.category.Category

internal class CategoryTypeMapping : SQLiteTypeMapping<Category>(
  CategoryPutResolver(),
  CategoryGetResolver(),
  CategoryDeleteResolver()
)

internal class CategoryPutResolver : DefaultPutResolver<Category>() {

  override fun mapToInsertQuery(obj: Category): InsertQuery {
    return InsertQuery.builder()
      .table(TABLE)
      .build()
  }

  override fun mapToUpdateQuery(obj: Category): UpdateQuery {
    return UpdateQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(obj.id)
      .build()
  }

  override fun mapToContentValues(obj: Category): ContentValues {
    return ContentValues(4).apply {
//      put(COL_ID, if (obj.id == -1L) null else obj.id)
      put(COL_ID, obj.id.takeIf { it != -1L })
      put(COL_NAME, obj.name)
      put(COL_ORDER, obj.order)
      put(COL_FLAGS, obj.flags)
    }
  }
}

internal class CategoryGetResolver : DefaultGetResolver<Category>() {

  override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): Category {
    val id = cursor.getLong(cursor.getColumnIndex(COL_ID))
    val name = cursor.getString(cursor.getColumnIndex(COL_NAME))
    val order = cursor.getInt(cursor.getColumnIndex(COL_ORDER))
    val flags = cursor.getInt(cursor.getColumnIndex(COL_FLAGS))

    return Category(id, name, order, flags)
  }
}

internal class CategoryDeleteResolver : DefaultDeleteResolver<Category>() {

  override fun mapToDeleteQuery(obj: Category): DeleteQuery {
    return DeleteQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(obj.id)
      .build()
  }
}
