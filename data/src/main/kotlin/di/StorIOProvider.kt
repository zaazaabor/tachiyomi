/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.di

import android.app.Application
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import tachiyomi.data.catalog.sql.CatalogTable
import tachiyomi.data.catalog.sql.CatalogTypeMapping
import tachiyomi.data.library.sql.CategoryTable
import tachiyomi.data.library.sql.CategoryTypeMapping
import tachiyomi.data.library.sql.MangaCategoryTable
import tachiyomi.data.library.sql.MangaCategoryTypeMapping
import tachiyomi.data.manga.sql.ChapterTable
import tachiyomi.data.manga.sql.ChapterTypeMapping
import tachiyomi.data.manga.sql.MangaTable
import tachiyomi.data.manga.sql.MangaTypeMapping
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.MangaCategory
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import toothpick.ProvidesSingletonInScope
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@ProvidesSingletonInScope
internal class StorIOProvider @Inject constructor(
  private val context: Application
) : Provider<StorIOSQLite> {

  override fun get(): StorIOSQLite {
    return DefaultStorIOSQLite.builder()
      .sqliteOpenHelper(getDbOpenHelper())
      .addTypeMapping(Manga::class.java, MangaTypeMapping())
      .addTypeMapping(Chapter::class.java, ChapterTypeMapping())
      .addTypeMapping(Category::class.java, CategoryTypeMapping())
      .addTypeMapping(MangaCategory::class.java, MangaCategoryTypeMapping())
      .addTypeMapping(CatalogRemote::class.java, CatalogTypeMapping())
      .build()
  }

  private fun getDbOpenHelper(): SupportSQLiteOpenHelper {
    val name = "tachiyomi.db"
    val version = 1
    val callbacks = listOf(
      MangaTable,
      ChapterTable,
      CategoryTable,
      MangaCategoryTable,
      CatalogTable
    )

    val callback = object : SupportSQLiteOpenHelper.Callback(version) {
      override fun onCreate(db: SupportSQLiteDatabase) {
        callbacks.forEach { it.onCreate(db) }
      }

      override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        callbacks.forEach { it.onUpgrade(db, oldVersion, newVersion) }
      }

      override fun onConfigure(db: SupportSQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
      }
    }

    val config = SupportSQLiteOpenHelper.Configuration.builder(context)
      .name(name)
      .callback(callback)
      .build()

    return RequerySQLiteOpenHelperFactory().create(config)
  }

}
