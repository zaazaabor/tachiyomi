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
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogSort
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class SubscribeLocalCatalogs @Inject constructor(
  private val catalogRepository: CatalogRepository,
  private val libraryRepository: LibraryRepository
) {

  fun interact(sort: CatalogSort = CatalogSort.Favorites) = Observable.defer {
    val catalogsFlow = Observables.combineLatest(
      catalogRepository.getInternalCatalogsObservable(),
      catalogRepository.getInstalledCatalogsObservable()
    ) { internal, installed ->
      internal + installed
    }

    when (sort) {
      CatalogSort.Name -> sortByName(catalogsFlow)
      CatalogSort.Favorites -> sortByFavorites(catalogsFlow)
    }
  }

  private fun sortByName(
    catalogsFlow: Observable<List<CatalogLocal>>
  ): Observable<List<CatalogLocal>> {
    return catalogsFlow.map { catalogs ->
      catalogs.sortedBy { it.name }
    }
  }

  private fun sortByFavorites(
    catalogsFlow: Observable<List<CatalogLocal>>
  ): Observable<List<CatalogLocal>> {
    var position = 0
    val favoriteIds = libraryRepository.findFavoriteSourceIds().associateWith { position++ }

    return catalogsFlow.map { catalogs ->
      catalogs.sortedWith(FavoritesComparator(favoriteIds).thenBy { it.name })
    }
  }

  private class FavoritesComparator(val favoriteIds: Map<Long, Int>) : Comparator<CatalogLocal> {

    override fun compare(c1: CatalogLocal, c2: CatalogLocal): Int {
      val pos1 = favoriteIds[c1.source.id] ?: Int.MAX_VALUE
      val pos2 = favoriteIds[c2.source.id] ?: Int.MAX_VALUE

      return pos1.compareTo(pos2)
    }

  }

}
