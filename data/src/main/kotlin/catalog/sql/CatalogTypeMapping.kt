/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.sql

import android.content.ContentValues
import android.database.Cursor
import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import tachiyomi.data.catalog.sql.CatalogTable.COL_APKURL
import tachiyomi.data.catalog.sql.CatalogTable.COL_DESCRIPTION
import tachiyomi.data.catalog.sql.CatalogTable.COL_ICONURL
import tachiyomi.data.catalog.sql.CatalogTable.COL_ID
import tachiyomi.data.catalog.sql.CatalogTable.COL_LANG
import tachiyomi.data.catalog.sql.CatalogTable.COL_NAME
import tachiyomi.data.catalog.sql.CatalogTable.COL_NSFW
import tachiyomi.data.catalog.sql.CatalogTable.COL_PKGNAME
import tachiyomi.data.catalog.sql.CatalogTable.COL_VCODE
import tachiyomi.data.catalog.sql.CatalogTable.COL_VNAME
import tachiyomi.data.catalog.sql.CatalogTable.TABLE
import tachiyomi.domain.catalog.model.CatalogRemote

internal class CatalogTypeMapping : SQLiteTypeMapping<CatalogRemote>(
  CatalogPutResolver(),
  CatalogGetResolver(),
  CatalogDeleteResolver()
)

internal class CatalogPutResolver : DefaultPutResolver<CatalogRemote>() {

  override fun mapToInsertQuery(obj: CatalogRemote): InsertQuery {
    return InsertQuery.builder()
      .table(TABLE)
      .build()
  }

  override fun mapToUpdateQuery(obj: CatalogRemote): UpdateQuery {
    return UpdateQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(obj.sourceId)
      .build()
  }

  override fun mapToContentValues(obj: CatalogRemote): ContentValues {
    return ContentValues(10).apply {
      put(COL_ID, obj.sourceId.takeIf { it != -1L })
      put(COL_NAME, obj.name)
      put(COL_DESCRIPTION, obj.description)
      put(COL_PKGNAME, obj.pkgName)
      put(COL_VCODE, obj.versionCode)
      put(COL_VNAME, obj.versionName)
      put(COL_LANG, obj.lang)
      put(COL_APKURL, obj.apkName)
      put(COL_ICONURL, obj.iconUrl)
      put(COL_NSFW, if (obj.nsfw) 1 else 0)
    }
  }

  override fun performPut(storIOSQLite: StorIOSQLite, obj: CatalogRemote): PutResult {
    val contentValues = mapToContentValues(obj)
    val insertQuery = mapToInsertQuery(obj)
    val insertedId = storIOSQLite.lowLevel().insert(insertQuery, contentValues)
    return PutResult.newInsertResult(insertedId, insertQuery.table(), insertQuery.affectsTags())
  }

}

internal class CatalogGetResolver : DefaultGetResolver<CatalogRemote>() {

  override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): CatalogRemote {
    val sourceId = cursor.getLong(cursor.getColumnIndex(COL_ID))
    val name = cursor.getString(cursor.getColumnIndex(COL_NAME))
    val description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION))
    val pkgName = cursor.getString(cursor.getColumnIndex(COL_PKGNAME))
    val versionCode = cursor.getInt(cursor.getColumnIndex(COL_VCODE))
    val versionName = cursor.getString(cursor.getColumnIndex(COL_VNAME))
    val lang = cursor.getString(cursor.getColumnIndex(COL_LANG))
    val apkUrl = cursor.getString(cursor.getColumnIndex(COL_APKURL))
    val iconUrl = cursor.getString(cursor.getColumnIndex(COL_ICONURL))
    val nsfw = cursor.getInt(cursor.getColumnIndex(COL_NSFW)) == 1

    return CatalogRemote(
      name, description, sourceId, pkgName, versionName, versionCode, lang, apkUrl, iconUrl, nsfw
    )
  }

}

internal class CatalogDeleteResolver : DefaultDeleteResolver<CatalogRemote>() {

  override fun mapToDeleteQuery(obj: CatalogRemote): DeleteQuery {
    return DeleteQuery.builder()
      .table(TABLE)
      .where("$COL_ID = ?")
      .whereArgs(obj.sourceId)
      .build()
  }

}
