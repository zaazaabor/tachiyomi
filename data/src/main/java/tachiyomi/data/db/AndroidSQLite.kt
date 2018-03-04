package tachiyomi.data.db

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import tachiyomi.data.category.table.CategoryTable
import javax.inject.Inject

internal class SQLite @Inject constructor(
  application: Application
) : SQLiteOpenHelper(application, "tachiyomi.db", null, 1) {

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(CategoryTable.createTableQuery)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
  }

}
