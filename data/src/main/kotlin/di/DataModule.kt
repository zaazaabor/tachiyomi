/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.di

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import tachiyomi.core.di.bindProvider
import tachiyomi.core.di.bindTo
import tachiyomi.data.catalog.CatalogRepositoryImpl
import tachiyomi.data.catalog.prefs.CatalogPreferences
import tachiyomi.data.catalog.prefs.CatalogPreferencesProvider
import tachiyomi.data.category.CategoryRepositoryImpl
import tachiyomi.data.chapter.ChapterRepositoryImpl
import tachiyomi.data.library.LibraryRepositoryImpl
import tachiyomi.data.library.prefs.LibraryPreferences
import tachiyomi.data.library.prefs.LibraryPreferencesProvider
import tachiyomi.data.manga.MangaRepositoryImpl
import tachiyomi.data.source.SourceManagerProvider
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.chapter.repository.ChapterRepository
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.domain.source.SourceManager
import toothpick.config.Module

object DataModule : Module() {

  init {
    bindProvider<StorIOSQLite, StorIOProvider>()

    bindProvider<SourceManager, SourceManagerProvider>()

    bindTo<MangaRepository, MangaRepositoryImpl>().singletonInScope()

    bindTo<ChapterRepository, ChapterRepositoryImpl>().singletonInScope()

    bindTo<CategoryRepository, CategoryRepositoryImpl>().singletonInScope()

    bindTo<LibraryRepository, LibraryRepositoryImpl>().singletonInScope()
    bindProvider<LibraryPreferences, LibraryPreferencesProvider>()

    bindTo<CatalogRepository, CatalogRepositoryImpl>().singletonInScope()
    bindProvider<CatalogPreferences, CatalogPreferencesProvider>()
  }

}
