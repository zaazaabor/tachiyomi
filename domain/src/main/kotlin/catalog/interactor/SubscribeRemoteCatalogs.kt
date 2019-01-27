/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class SubscribeRemoteCatalogs @Inject constructor(
  private val catalogRepository: CatalogRepository
) {

  fun interact(
    excludeInstalled: Boolean = false,
    withNsfw: Boolean = true
  ): Observable<List<CatalogRemote>> {
    val remoteCatalogsObservable = if (excludeInstalled) {
      val installedPkgsObservable = catalogRepository.getInstalledCatalogsObservable()
        .map { catalogs -> catalogs.asSequence().map { it.pkgName }.toSet() }

      Observables.combineLatest(
        installedPkgsObservable,
        catalogRepository.getRemoteCatalogsObservable()
      ) { installedPkgs, remoteCatalogs ->
        remoteCatalogs.filter { it.pkgName !in installedPkgs }
      }
    } else {
      catalogRepository.getRemoteCatalogsObservable()
    }

    return remoteCatalogsObservable
      .map { catalogs ->
        if (withNsfw) {
          catalogs
        } else {
          catalogs.filter { !it.nsfw }
        }
      }
  }

}
