package tachiyomi.data.manga.resolver

import android.content.ContentValues
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.put.PutResolver
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.domain.manga.model.Manga

internal class MangaFlagsPutResolver : PutResolver<Manga>() {

  override fun performPut(db: StorIOSQLite, manga: Manga): PutResult {
    val updateQuery = mapToUpdateQuery(manga)
    val contentValues = mapToContentValues(manga)

    val numberOfRowsUpdated = db.lowLevel().update(updateQuery, contentValues)
    return PutResult.newUpdateResult(numberOfRowsUpdated, updateQuery.table())
  }

  fun mapToUpdateQuery(manga: Manga): UpdateQuery {
    return UpdateQuery.builder()
      .table(MangaTable.TABLE)
      .where("${MangaTable.COL_ID} = ?")
      .whereArgs(manga.id)
      .build()
  }

  fun mapToContentValues(manga: Manga): ContentValues {
    return ContentValues(1).apply {
      put(MangaTable.COL_FLAGS, manga.flags)
    }
  }

}

