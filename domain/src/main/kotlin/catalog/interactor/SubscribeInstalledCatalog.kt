/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import io.reactivex.Observable
import tachiyomi.core.stdlib.Optional
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class SubscribeInstalledCatalog @Inject constructor(
  private val catalogRepository: CatalogRepository
) {

  fun interact(pkgName: String): Observable<Optional<CatalogInstalled>> {
    return catalogRepository.getInstalledCatalogsObservable()
      .map { catalogs ->
        val catalog = catalogs.find { it.pkgName == pkgName }
        Optional.of(catalog)
      }
      .distinctUntilChanged()
  }

}
