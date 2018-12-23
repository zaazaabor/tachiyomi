/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.queries.RawQuery
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import tachiyomi.data.category.table.CategoryTable
import tachiyomi.data.category.table.MangaCategoryTable
import tachiyomi.data.chapter.table.ChapterTable
import tachiyomi.data.library.resolver.FavoriteSourceIdsGetResolver
import tachiyomi.data.library.resolver.LibraryMangaGetResolver
import tachiyomi.data.manga.resolver.MangaFavoritePutResolver
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject

internal class LibraryRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite,
  private val categoryRepository: CategoryRepository
) : LibraryRepository {

//  override fun getLibraryManga(): Flowable<List<Manga>> {
//    return storio.get()
//      .listOfObjects(Manga::class.java)
//      .withQuery(Query.builder()
//        .table(MangaTable.TABLE)
//        .where("${MangaTable.COL_FAVORITE} = ?")
//        .whereArgs(1)
//        .orderBy(MangaTable.COL_TITLE)
//        .build())
//      .prepare()
//      .asRxFlowable(BackpressureStrategy.BUFFER)
//  }

  override fun getLibraryMangas(): Flowable<List<LibraryManga>> {
    return storio.get()
      .listOfObjects(LibraryManga::class.java)
      .withQuery(RawQuery.builder()
        .query(LibraryMangaGetResolver.query)
        .observesTables(MangaTable.TABLE, ChapterTable.TABLE,
          MangaCategoryTable.TABLE, CategoryTable.TABLE)
        .build())
      .withGetResolver(LibraryMangaGetResolver)
      .prepare()
      .asRxFlowable(BackpressureStrategy.BUFFER)
  }

  override fun getFavoriteSourceIds(): Single<List<Long>> {
    return storio.get()
      .listOfObjects(Long::class.java)
      .withQuery(RawQuery.builder().query(FavoriteSourceIdsGetResolver.query).build())
      .withGetResolver(FavoriteSourceIdsGetResolver)
      .prepare()
      .asRxSingle()
  }

  override fun addToLibrary(manga: Manga): Completable {
    return setFavorite(manga, true)
  }

  override fun removeFromLibrary(manga: Manga): Completable {
    return setFavorite(manga, false)
  }

  private fun setFavorite(manga: Manga, favorite: Boolean): Completable {
    return storio.put()
      .`object`(manga.copy(favorite = favorite))
      .withPutResolver(MangaFavoritePutResolver())
      .prepare()
      .asRxCompletable()
  }
}
