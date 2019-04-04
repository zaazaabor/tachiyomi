/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.repository

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetListOfObjects
import com.pushtorefresh.storio3.sqlite.queries.RawQuery
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import tachiyomi.core.db.asBlocking
import tachiyomi.data.library.sql.FavoriteSourceIdsGetResolver
import tachiyomi.data.library.sql.LibraryMangaGetResolver
import tachiyomi.data.library.sql.MangaCategoryTable
import tachiyomi.data.manga.sql.ChapterTable
import tachiyomi.data.manga.sql.MangaTable
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

internal class LibraryRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : LibraryRepository {

  private fun preparedAll(): PreparedGetListOfObjects<LibraryManga> {
    return storio.get()
      .listOfObjects(LibraryManga::class.java)
      .withQuery(RawQuery.builder()
        .query(LibraryMangaGetResolver.allQuery)
        .observesTables(MangaTable.TABLE, ChapterTable.TABLE)
        .build())
      .withGetResolver(LibraryMangaGetResolver)
      .prepare()
  }

  private fun preparedUncategorized(): PreparedGetListOfObjects<LibraryManga> {
    return storio.get()
      .listOfObjects(LibraryManga::class.java)
      .withQuery(RawQuery.builder()
        .query(LibraryMangaGetResolver.uncategorizedQuery)
        .observesTables(MangaTable.TABLE, ChapterTable.TABLE, MangaCategoryTable.TABLE)
        .build())
      .withGetResolver(LibraryMangaGetResolver)
      .prepare()
  }

  private fun preparedToCategory(categoryId: Long): PreparedGetListOfObjects<LibraryManga> {
    return storio.get()
      .listOfObjects(LibraryManga::class.java)
      .withQuery(RawQuery.builder()
        .query(LibraryMangaGetResolver.categoryQuery)
        .args(categoryId)
        .observesTables(MangaTable.TABLE, ChapterTable.TABLE, MangaCategoryTable.TABLE)
        .build())
      .withGetResolver(LibraryMangaGetResolver)
      .prepare()
  }

  override fun subscribeAll(): Observable<List<LibraryManga>> {
    return preparedAll().asRxFlowable(BackpressureStrategy.LATEST).toObservable()
  }

  override fun subscribeUncategorized(): Observable<List<LibraryManga>> {
    return preparedUncategorized().asRxFlowable(BackpressureStrategy.LATEST).toObservable()
  }

  override fun subscribeToCategory(categoryId: Long): Observable<List<LibraryManga>> {
    return preparedToCategory(categoryId).asRxFlowable(BackpressureStrategy.LATEST).toObservable()
  }

  override fun findAll(): List<LibraryManga> {
    return preparedAll().asBlocking()
  }

  override fun findUncategorized(): List<LibraryManga> {
    return preparedUncategorized().asBlocking()
  }

  override fun findToCategory(categoryId: Long): List<LibraryManga> {
    return preparedToCategory(categoryId).asBlocking()
  }

  override fun findFavoriteSourceIds(): List<Long> {
    return storio.get()
      .listOfObjects(Long::class.java)
      .withQuery(RawQuery.builder().query(FavoriteSourceIdsGetResolver.query).build())
      .withGetResolver(FavoriteSourceIdsGetResolver)
      .prepare()
      .asBlocking()
  }

}
