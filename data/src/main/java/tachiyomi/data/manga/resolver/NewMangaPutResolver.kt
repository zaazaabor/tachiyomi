package tachiyomi.data.manga.resolver

import android.content.ContentValues
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.domain.manga.model.Manga

internal class NewMangaPutResolver : DefaultPutResolver<Manga>() {

  override fun mapToInsertQuery(obj: Manga): InsertQuery {
    return InsertQuery.builder()
      .table(MangaTable.TABLE)
      .build()
  }

  override fun mapToUpdateQuery(obj: Manga): UpdateQuery {
    throw Exception("This resolver is only used for insertions!")
  }

  override fun mapToContentValues(obj: Manga): ContentValues {
    return ContentValues(14).apply {
      put(MangaTable.COL_SOURCE, obj.source)
      put(MangaTable.COL_URL, obj.key)
      put(MangaTable.COL_ARTIST, obj.artist)
      put(MangaTable.COL_AUTHOR, obj.author)
      put(MangaTable.COL_DESCRIPTION, obj.description)
      put(MangaTable.COL_GENRE, obj.genres)
      put(MangaTable.COL_TITLE, obj.title)
      put(MangaTable.COL_STATUS, obj.status)
      put(MangaTable.COL_COVER, obj.cover)
      put(MangaTable.COL_FAVORITE, obj.favorite)
      put(MangaTable.COL_LAST_UPDATE, obj.lastUpdate)
      put(MangaTable.COL_INITIALIZED, obj.initialized)
      put(MangaTable.COL_VIEWER, obj.viewer)
      put(MangaTable.COL_FLAGS, obj.flags)
    }
  }
}
