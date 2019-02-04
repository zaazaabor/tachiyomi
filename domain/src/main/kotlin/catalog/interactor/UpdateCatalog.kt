/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import io.reactivex.Observable
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.InstallStep
import tachiyomi.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class UpdateCatalog @Inject constructor(
  private val catalogRepository: CatalogRepository,
  private val installCatalog: InstallCatalog
) {

  fun interact(catalog: CatalogInstalled): Observable<InstallStep> {
    return catalogRepository.getRemoteCatalogsObservable()
      .firstOrError()
      .map { catalogs ->
        catalogs.find { it.pkgName == catalog.pkgName }
          ?: throw Exception("Catalog with pkg ${catalog.pkgName} not found")
      }
      .flatMapObservable { installCatalog.interact(it) }
  }

}
