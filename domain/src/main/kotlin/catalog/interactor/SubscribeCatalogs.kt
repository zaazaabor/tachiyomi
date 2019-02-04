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
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogSort
import javax.inject.Inject

class SubscribeCatalogs @Inject constructor(
  private val localCatalogs: SubscribeLocalCatalogs,
  private val remoteCatalogs: SubscribeRemoteCatalogs
) {

  fun interact(
    sort: CatalogSort = CatalogSort.Favorites,
    excludeRemoteInstalled: Boolean = false,
    withNsfw: Boolean = true
  ) = Observable.defer {
    Observables.combineLatest(
      localCatalogs.interact(sort),
      remoteCatalogs.interact(withNsfw = withNsfw)
    ).map { localAndRemote ->
      val (local, remote) = localAndRemote
      if (excludeRemoteInstalled) {
        val installedPkgs = local
          .asSequence()
          .filterIsInstance<CatalogInstalled>()
          .map { it.pkgName }
          .toSet()

        local to remote.filter { it.pkgName !in installedPkgs }
      } else {
        localAndRemote
      }
    }
  }

}
