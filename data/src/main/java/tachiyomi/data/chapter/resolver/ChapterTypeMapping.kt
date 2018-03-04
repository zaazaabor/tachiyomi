package tachiyomi.data.chapter.resolver

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
import tachiyomi.data.chapter.model.Chapter

internal class ChapterTypeMapping : SQLiteTypeMapping<Chapter>(
  ChapterPutResolver(),
  ChapterGetResolver(),
  ChapterDeleteResolver()
)

internal class ChapterPutResolver : DefaultPutResolver<Chapter>() {

  override fun mapToInsertQuery(obj: Chapter) = InsertQuery.builder()
    .table("")
    .build()

  override fun mapToUpdateQuery(obj: Chapter) = UpdateQuery.builder()
    .table("")
    .build()

  override fun mapToContentValues(obj: Chapter) = ContentValues().apply {

  }
}

internal class ChapterGetResolver : DefaultGetResolver<Chapter>() {
  override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): Chapter {
    return Chapter()
  }
}

internal class ChapterDeleteResolver : DefaultDeleteResolver<Chapter>() {

  override fun mapToDeleteQuery(obj: Chapter) = DeleteQuery.builder()
    .table("")
    .build()
}
