/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.di

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import tachiyomi.core.db.StorIOTransaction
import tachiyomi.core.db.Transaction
import tachiyomi.core.di.bindProvider
import tachiyomi.core.di.bindTo
import tachiyomi.data.catalog.installer.CatalogInstaller
import tachiyomi.data.catalog.prefs.CatalogPreferences
import tachiyomi.data.catalog.prefs.CatalogPreferencesProvider
import tachiyomi.data.catalog.repository.CatalogRepositoryImpl
import tachiyomi.data.category.repository.CategoryRepositoryImpl
import tachiyomi.data.category.repository.MangaCategoryRepositoryImpl
import tachiyomi.data.chapter.repository.ChapterRepositoryImpl
import tachiyomi.data.library.prefs.LibraryPreferencesProvider
import tachiyomi.data.library.repository.LibraryCoversImpl
import tachiyomi.data.library.repository.LibraryRepositoryImpl
import tachiyomi.data.library.updater.LibraryUpdaterImpl
import tachiyomi.data.manga.repository.MangaRepositoryImpl
import tachiyomi.data.source.SourceManagerProvider
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.category.repository.MangaCategoryRepository
import tachiyomi.domain.chapter.repository.ChapterRepository
import tachiyomi.domain.library.prefs.LibraryPreferences
import tachiyomi.domain.library.repository.LibraryCovers
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.library.updater.LibraryUpdater
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.domain.source.SourceManager
import toothpick.config.Module

object DataModule : Module() {

  init {
    bindProvider<StorIOSQLite, StorIOProvider>()
    bindTo<Transaction, StorIOTransaction>()

    bindProvider<SourceManager, SourceManagerProvider>()

    bindTo<MangaRepository, MangaRepositoryImpl>().singletonInScope()

    bindTo<ChapterRepository, ChapterRepositoryImpl>().singletonInScope()

    bindTo<CategoryRepository, CategoryRepositoryImpl>().singletonInScope()
    bindTo<MangaCategoryRepository, MangaCategoryRepositoryImpl>().singletonInScope()

    bindTo<LibraryRepository, LibraryRepositoryImpl>().singletonInScope()
    bindProvider<LibraryPreferences, LibraryPreferencesProvider>()
    bindTo<LibraryCovers, LibraryCoversImpl>().singletonInScope()
    bindTo<LibraryUpdater, LibraryUpdaterImpl>().singletonInScope()

    bind(CatalogInstaller::class.java).singletonInScope()
    bindTo<CatalogRepository, CatalogRepositoryImpl>().singletonInScope()
    bindProvider<CatalogPreferences, CatalogPreferencesProvider>()
  }

}
