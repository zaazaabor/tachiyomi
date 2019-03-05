/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.di

import android.app.Application
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite
import tachiyomi.core.db.DbOpenHelper
import tachiyomi.data.catalog.sql.CatalogTable
import tachiyomi.data.catalog.sql.CatalogTypeMapping
import tachiyomi.data.category.model.MangaCategory
import tachiyomi.data.category.resolver.CategoryTypeMapping
import tachiyomi.data.category.resolver.MangaCategoryTypeMapping
import tachiyomi.data.category.table.CategoryTable
import tachiyomi.data.category.table.MangaCategoryTable
import tachiyomi.data.chapter.resolver.ChapterTypeMapping
import tachiyomi.data.chapter.table.ChapterTable
import tachiyomi.data.manga.resolver.MangaTypeMapping
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.category.Category
import tachiyomi.domain.chapter.model.Chapter
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

  private fun getDbOpenHelper(): DbOpenHelper {
    val name = "tachiyomi.db"
    val version = 1
    val callbacks = listOf(
      MangaTable,
      ChapterTable,
      CategoryTable,
      MangaCategoryTable,
      CatalogTable
    )

    return DbOpenHelper(context, name, version, callbacks)
  }

}
