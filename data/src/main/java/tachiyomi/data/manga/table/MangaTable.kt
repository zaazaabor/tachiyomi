package tachiyomi.data.manga.table

import android.database.sqlite.SQLiteDatabase
import tachiyomi.core.db.DbOpenCallback

internal object MangaTable : DbOpenCallback {

  const val TABLE = "manga"

  const val COL_ID = "m_id"
  const val COL_SOURCE = "m_source"
  const val COL_KEY = "m_url"
  const val COL_TITLE = "m_title"
  const val COL_ARTIST = "m_artist"
  const val COL_AUTHOR = "m_author"
  const val COL_DESCRIPTION = "m_description"
  const val COL_GENRE = "m_genre"
  const val COL_STATUS = "m_status"
  const val COL_COVER = "m_cover"
  const val COL_FAVORITE = "m_favorite"
  const val COL_LAST_UPDATE = "m_last_update"
  const val COL_INITIALIZED = "m_initialized"
  const val COL_VIEWER = "m_viewer"
  const val COL_FLAGS = "m_flags"

  private val createTableQuery: String
    get() = """CREATE TABLE $TABLE(
            $COL_ID INTEGER NOT NULL PRIMARY KEY,
            $COL_SOURCE INTEGER NOT NULL,
            $COL_KEY TEXT NOT NULL,
            $COL_TITLE TEXT NOT NULL,
            $COL_ARTIST TEXT,
            $COL_AUTHOR TEXT,
            $COL_DESCRIPTION TEXT,
            $COL_GENRE TEXT,
            $COL_STATUS INTEGER NOT NULL,
            $COL_COVER TEXT,
            $COL_FAVORITE INTEGER NOT NULL,
            $COL_LAST_UPDATE LONG,
            $COL_INITIALIZED BOOLEAN NOT NULL,
            $COL_VIEWER INTEGER NOT NULL,
            $COL_FLAGS INTEGER NOT NULL
            )"""

  val createUrlIndexQuery: String
    get() = "CREATE INDEX ${TABLE}_${COL_KEY}_index ON $TABLE($COL_KEY)"

  val createFavoriteIndexQuery: String
    get() = "CREATE INDEX ${TABLE}_${COL_FAVORITE}_index ON $TABLE($COL_FAVORITE)"

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(createTableQuery)
  }

}
