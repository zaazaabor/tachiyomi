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
import tachiyomi.data.category.table.MangaCategoryTable
import tachiyomi.data.chapter.table.ChapterTable
import tachiyomi.data.library.resolver.FavoriteSourceIdsGetResolver
import tachiyomi.data.library.resolver.LibraryMangaForCategoryGetResolver
import tachiyomi.data.library.resolver.LibraryMangaGetResolver
import tachiyomi.data.manga.resolver.MangaFavoritePutResolver
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject

internal class LibraryRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : LibraryRepository {

  override fun getLibraryMangas(): Flowable<List<LibraryManga>> {
    return storio.get()
      .listOfObjects(LibraryManga::class.java)
      .withQuery(RawQuery.builder()
        .query(LibraryMangaGetResolver.query)
        .observesTables(MangaTable.TABLE, ChapterTable.TABLE)
        .build())
      .withGetResolver(LibraryMangaGetResolver)
      .prepare()
      .asRxFlowable(BackpressureStrategy.LATEST)
  }

  override fun getLibraryMangasForCategory(categoryId: Long): Flowable<List<LibraryManga>> {
    return storio.get()
      .listOfObjects(LibraryManga::class.java)
      .withQuery(RawQuery.builder()
        .query(LibraryMangaForCategoryGetResolver.query)
        .args(categoryId)
        .observesTables(MangaTable.TABLE, ChapterTable.TABLE, MangaCategoryTable.TABLE)
        .build())
      .withGetResolver(LibraryMangaForCategoryGetResolver)
      .prepare()
      .asRxFlowable(BackpressureStrategy.LATEST)
  }

  override fun getFavoriteSourceIds(): Single<List<Long>> {
    return storio.get()
      .listOfObjects(Long::class.java)
      .withQuery(RawQuery.builder().query(FavoriteSourceIdsGetResolver.query).build())
      .withGetResolver(FavoriteSourceIdsGetResolver)
      .prepare()
      .asRxSingle()
  }

  override fun updateFavorite(manga: Manga): Completable {
    return Completable.fromAction {
      storio.put()
        .`object`(manga)
        .withPutResolver(MangaFavoritePutResolver())
        .prepare()
        .executeAsBlocking()
    }
  }

}
