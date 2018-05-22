package tachiyomi.data.chapter.table

import android.database.sqlite.SQLiteDatabase
import tachiyomi.core.db.DbOpenCallback
import tachiyomi.data.manga.table.MangaTable

internal object ChapterTable : DbOpenCallback {

  const val TABLE = "chapters"

  const val COL_ID = "c_id"
  const val COL_MANGA_ID = "c_manga_id"
  const val COL_KEY = "c_url"
  const val COL_NAME = "c_name"
  const val COL_READ = "c_read"
  const val COL_SCANLATOR = "c_scanlator"
  const val COL_BOOKMARK = "c_bookmark"
  const val COL_DATE_FETCH = "c_date_fetch"
  const val COL_DATE_UPLOAD = "c_date_upload"
  const val COL_PROGRESS = "c_last_page_read"
  const val COL_NUMBER = "c_number"
  const val COL_SOURCE_ORDER = "source_order"

  private val createTableQuery: String
    get() = """CREATE TABLE $TABLE(
            $COL_ID INTEGER NOT NULL PRIMARY KEY,
            $COL_MANGA_ID INTEGER NOT NULL,
            $COL_KEY TEXT NOT NULL,
            $COL_NAME TEXT NOT NULL,
            $COL_SCANLATOR TEXT,
            $COL_READ BOOLEAN NOT NULL,
            $COL_BOOKMARK BOOLEAN NOT NULL,
            $COL_PROGRESS INT NOT NULL,
            $COL_NUMBER FLOAT NOT NULL,
            $COL_SOURCE_ORDER INTEGER NOT NULL,
            $COL_DATE_FETCH LONG NOT NULL,
            $COL_DATE_UPLOAD LONG NOT NULL,
            FOREIGN KEY($COL_MANGA_ID) REFERENCES ${MangaTable.TABLE} (${MangaTable.COL_ID})
            ON DELETE CASCADE
            )"""

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(createTableQuery)
  }

}
