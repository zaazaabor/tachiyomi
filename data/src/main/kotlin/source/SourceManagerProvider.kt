/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.source

import android.app.Application
import tachiyomi.data.catalog.repository.CatalogRepositoryImpl
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.domain.source.SourceManager
import toothpick.ProvidesSingletonInScope
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@ProvidesSingletonInScope
internal class SourceManagerProvider @Inject constructor(
  private val context: Application,
  private val catalogRepository: CatalogRepository
) : Provider<SourceManager> {

  override fun get(): SourceManager {
    val sourceManager = SourceManagerImpl(context)
    (catalogRepository as CatalogRepositoryImpl).init(sourceManager)
    return sourceManager
  }

}
