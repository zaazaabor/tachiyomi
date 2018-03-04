package tachiyomi.data.di

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import tachiyomi.data.db.SQLite
import tachiyomi.data.db.wrapDb
import javax.inject.Inject
import javax.inject.Provider

internal class StorioProvider @Inject constructor(
  private val sql: SQLite
) : Provider<StorIOSQLite> {

  override fun get(): StorIOSQLite {
    return sql.wrapDb()
  }
}
