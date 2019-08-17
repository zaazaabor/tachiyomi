/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class GetInstalledCatalog @Inject constructor(
  private val catalogRepository: CatalogRepository
) {

  fun get(pkgName: String): CatalogInstalled? {
    return catalogRepository.installedCatalogs.find { it.pkgName == pkgName }
  }

  fun subscribe(pkgName: String): Flow<CatalogInstalled?> {
    return catalogRepository.getInstalledCatalogsFlow()
      .map { catalogs ->
        catalogs.find { it.pkgName == pkgName }
      }
      .distinctUntilChanged()
  }

}
