/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.repository

import io.reactivex.Completable
import io.reactivex.Observable
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogInternal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep

interface CatalogRepository {

  val installedCatalogs: List<CatalogInstalled>

  fun getInternalCatalogsObservable(): Observable<List<CatalogInternal>>

  fun getInstalledCatalogsObservable(): Observable<List<CatalogInstalled>>

  fun getRemoteCatalogsObservable(): Observable<List<CatalogRemote>>

  fun installCatalog(catalog: CatalogRemote): Observable<InstallStep>

  fun uninstallCatalog(catalog: CatalogInstalled): Completable

}
