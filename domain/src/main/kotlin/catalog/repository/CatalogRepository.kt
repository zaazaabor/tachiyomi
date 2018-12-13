/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.repository

import io.reactivex.Flowable
import tachiyomi.domain.catalog.model.Catalog

interface CatalogRepository {

  fun getBuiltInCatalogsFlowable(): Flowable<List<Catalog.BuiltIn>>

  fun getInstalledCatalogsFlowable(): Flowable<List<Catalog.Installed>>

  fun getAvailableCatalogsFlowable(): Flowable<List<Catalog.Available>>
}
