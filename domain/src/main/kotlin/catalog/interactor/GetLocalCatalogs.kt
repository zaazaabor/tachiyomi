/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.interactor

import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogSort
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class GetLocalCatalogs @Inject constructor(
  private val catalogRepository: CatalogRepository,
  private val libraryRepository: LibraryRepository
) {

  fun interact(sort: CatalogSort = CatalogSort.Favorites) = Flowable.defer {
    val catalogsFlow = Flowables.combineLatest(
      catalogRepository.getInternalCatalogsFlowable(),
      catalogRepository.getInstalledCatalogsFlowable()
    ) { internal, installed ->
      internal + installed
    }

    when (sort) {
      CatalogSort.Name -> sortByName(catalogsFlow)
      CatalogSort.Favorites -> sortByFavorites(catalogsFlow)
    }
  }

  private fun sortByName(catalogsFlow: Flowable<List<CatalogLocal>>): Flowable<List<CatalogLocal>> {
    return catalogsFlow.map { catalogs ->
      catalogs.sortedWith(UpdatesComparator().thenBy { it.name })
    }
  }

  private fun sortByFavorites(
    catalogsFlow: Flowable<List<CatalogLocal>>
  ): Flowable<List<CatalogLocal>> {
    val favoriteIdsFlow = libraryRepository.getFavoriteSourceIds()
      .map { favoriteIds ->
        var position = 0
        favoriteIds.associateWith { position++ }
      }
      .toFlowable()

    return Flowables.combineLatest(
      catalogsFlow,
      favoriteIdsFlow
    ) { catalogs, favoriteIds ->
      catalogs.sortedWith(
        UpdatesComparator().then(FavoritesComparator(favoriteIds)).thenBy { it.name }
      )
    }
  }

  private class FavoritesComparator(val favoriteIds: Map<Long, Int>) : Comparator<CatalogLocal> {

    override fun compare(c1: CatalogLocal, c2: CatalogLocal): Int {
      val pos1 = favoriteIds[c1.source.id] ?: Int.MAX_VALUE
      val pos2 = favoriteIds[c2.source.id] ?: Int.MAX_VALUE

      return pos1.compareTo(pos2)
    }

  }

  private class UpdatesComparator : Comparator<CatalogLocal> {

    override fun compare(c1: CatalogLocal, c2: CatalogLocal): Int {
      return when {
        c1 is CatalogInstalled && c2 is CatalogInstalled -> c2.hasUpdate.compareTo(c1.hasUpdate)
        else -> 0
      }
    }

  }

}
