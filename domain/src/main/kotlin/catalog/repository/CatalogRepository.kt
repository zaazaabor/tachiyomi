/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.repository

import io.reactivex.Flowable
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogInternal
import tachiyomi.domain.catalog.model.CatalogRemote

interface CatalogRepository {

  val installedCatalogs: List<CatalogInstalled>

  fun getInternalCatalogsFlowable(): Flowable<List<CatalogInternal>>

  fun getInstalledCatalogsFlowable(): Flowable<List<CatalogInstalled>>

  fun getRemoteCatalogsFlowable(): Flowable<List<CatalogRemote>>
}
