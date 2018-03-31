package tachiyomi.data.library.resolver

import android.database.Cursor
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver
import tachiyomi.data.category.table.MangaCategoryTable
import tachiyomi.data.chapter.table.ChapterTable
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.domain.library.LibraryEntry
import tachiyomi.domain.manga.model.Manga

internal object LibraryMangaGetResolver : DefaultGetResolver<LibraryEntry>() {

  /**
   * Query to get the manga from the library, with their categories and unread count.
   */
  val query = """
    SELECT M.*, COALESCE(MC.${MangaCategoryTable.COL_CATEGORY_ID}, 0) AS a_category
    FROM (
        SELECT ${MangaTable.TABLE}.*, COALESCE(C.unread, 0) AS a_unread
        FROM ${MangaTable.TABLE}
        LEFT JOIN (
            SELECT ${ChapterTable.COL_MANGA_ID}, COUNT(*) AS unread
            FROM ${ChapterTable.TABLE}
            WHERE ${ChapterTable.COL_READ} = 0
            GROUP BY ${ChapterTable.COL_MANGA_ID}
        ) AS C
        ON ${MangaTable.COL_ID} = C.${ChapterTable.COL_MANGA_ID}
        WHERE ${MangaTable.COL_FAVORITE} = 1
        GROUP BY ${MangaTable.COL_ID}
        ORDER BY ${MangaTable.COL_TITLE}
    ) AS M
    LEFT JOIN (
        SELECT * FROM ${MangaCategoryTable.TABLE}) AS MC
        ON MC.${MangaCategoryTable.COL_MANGA_ID} = M.${MangaTable.COL_ID}
  """

  override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): LibraryEntry {
    val id = cursor.getLong(cursor.getColumnIndex(MangaTable.COL_ID))
    val source = cursor.getLong(cursor.getColumnIndex(MangaTable.COL_SOURCE))
    val url = cursor.getString(cursor.getColumnIndex(MangaTable.COL_KEY))
    val artist = cursor.getString(cursor.getColumnIndex(MangaTable.COL_ARTIST))
    val author = cursor.getString(cursor.getColumnIndex(MangaTable.COL_AUTHOR))
    val description = cursor.getString(cursor.getColumnIndex(MangaTable.COL_DESCRIPTION))
    val genre = cursor.getString(cursor.getColumnIndex(MangaTable.COL_GENRE))
    val title = cursor.getString(cursor.getColumnIndex(MangaTable.COL_TITLE))
    val status = cursor.getInt(cursor.getColumnIndex(MangaTable.COL_STATUS))
    val cover = cursor.getString(cursor.getColumnIndex(MangaTable.COL_COVER))
    val favorite = cursor.getInt(cursor.getColumnIndex(MangaTable.COL_FAVORITE)) == 1
    val lastUpdate = cursor.getLong(cursor.getColumnIndex(MangaTable.COL_LAST_UPDATE))
    val initialized = cursor.getInt(cursor.getColumnIndex(MangaTable.COL_INITIALIZED)) == 1
    val viewer = cursor.getInt(cursor.getColumnIndex(MangaTable.COL_VIEWER))
    val flags = cursor.getInt(cursor.getColumnIndex(MangaTable.COL_FLAGS))

    val manga = Manga(id, source, url, artist, author, description, genre, title,
      status, cover, favorite, lastUpdate, initialized, viewer, flags)

    val category = cursor.getLong(cursor.getColumnIndex("a_category"))
    val unread = cursor.getInt(cursor.getColumnIndex("a_unread"))

    return LibraryEntry(manga, category, unread)
  }

}
