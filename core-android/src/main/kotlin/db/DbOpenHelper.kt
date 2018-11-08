package tachiyomi.core.db

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Generic class to instantiate a database. It receives an application [context], the [name] of
 * the database (used as filename), the current [version] of the database, and a list of [callbacks]
 * to execute when this database is created or updated.
 */
class DbOpenHelper(
  context: Application,
  name: String,
  version: Int,
  private val callbacks: List<DbOpenCallback>
) : SQLiteOpenHelper(context, name, null, version) {

  /**
   * Called when the [db] connection is being configured.
   */
  override fun onConfigure(db: SQLiteDatabase) {
    db.setForeignKeyConstraintsEnabled(true)
  }

  /**
   * Called when the [db] is created for the first time. It delegates everything to the registered
   * [callbacks].
   */
  override fun onCreate(db: SQLiteDatabase) {
    callbacks.forEach { it.onCreate(db) }
  }

  /**
   * Called when the [db] needs to be upgraded from [oldVersion] to [newVersion]. It delegates
   * everything to the registered [callbacks].
   */
  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    callbacks.forEach { it.onUpgrade(db, oldVersion, newVersion) }
  }

}
