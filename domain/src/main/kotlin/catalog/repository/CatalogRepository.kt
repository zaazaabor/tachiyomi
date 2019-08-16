/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogInternal
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep

interface CatalogRepository {

  val installedCatalogs: List<CatalogInstalled>

  fun get(sourceId: Long): CatalogLocal?

  fun getInternalCatalogsFlow(): Flow<List<CatalogInternal>>

  fun getInstalledCatalogsFlow(): Flow<List<CatalogInstalled>>

  fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>>

  suspend fun refreshRemoteCatalogs(forceRefresh: Boolean)

  fun installCatalog(catalog: CatalogRemote): Flow<InstallStep>

  suspend fun uninstallCatalog(catalog: CatalogInstalled)

}
