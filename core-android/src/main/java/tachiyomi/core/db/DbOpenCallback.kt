package tachiyomi.core.db

import android.database.sqlite.SQLiteDatabase

/**
 * An interface to receive an event when the database is created or upgraded.
 */
interface DbOpenCallback {

  /**
   * Called when the [db] is created for the first time.
   */
  fun onCreate(db: SQLiteDatabase)

  /**
   * Called when the [db] needs to be upgraded from [oldVersion] to [newVersion]. The implementation
   * of this method is optional.
   */
  fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

}
