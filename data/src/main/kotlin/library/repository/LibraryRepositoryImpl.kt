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
import kotlinx.coroutines.flow.Flow
import tachiyomi.core.db.asBlocking
import tachiyomi.core.rx.asFlow
import tachiyomi.data.library.sql.FavoriteSourceIdsGetResolver
import tachiyomi.data.library.sql.LibraryMangaGetResolver
import tachiyomi.data.library.sql.MangaCategoryTable
import tachiyomi.data.manga.sql.ChapterTable
import tachiyomi.data.manga.sql.MangaTable
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySorting
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

internal class LibraryRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : LibraryRepository {

  private fun preparedAll(sort: LibrarySorting): PreparedGetListOfObjects<LibraryManga> {
    return storio.get()
      .listOfObjects(LibraryManga::class.java)
      .withQuery(RawQuery.builder()
        .query(LibraryMangaGetResolver.getAllQuery(sort))
        .observesTables(MangaTable.TABLE, ChapterTable.TABLE)
        .build())
      .withGetResolver(LibraryMangaGetResolver)
      .prepare()
  }

  private fun preparedUncategorized(sort: LibrarySorting): PreparedGetListOfObjects<LibraryManga> {
    return storio.get()
      .listOfObjects(LibraryManga::class.java)
      .withQuery(RawQuery.builder()
        .query(LibraryMangaGetResolver.getUncategorizedQuery(sort))
        .observesTables(MangaTable.TABLE, ChapterTable.TABLE, MangaCategoryTable.TABLE)
        .build())
      .withGetResolver(LibraryMangaGetResolver)
      .prepare()
  }

  private fun preparedToCategory(
    categoryId: Long,
    sort: LibrarySorting
  ): PreparedGetListOfObjects<LibraryManga> {
    return storio.get()
      .listOfObjects(LibraryManga::class.java)
      .withQuery(RawQuery.builder()
        .query(LibraryMangaGetResolver.getCategoryQuery(sort))
        .args(categoryId)
        .observesTables(MangaTable.TABLE, ChapterTable.TABLE, MangaCategoryTable.TABLE)
        .build())
      .withGetResolver(LibraryMangaGetResolver)
      .prepare()
  }

  override fun subscribeAll(sort: LibrarySorting): Flow<List<LibraryManga>> {
    return preparedAll(sort).asRxFlowable(BackpressureStrategy.LATEST).asFlow()
  }

  override fun subscribeUncategorized(sort: LibrarySorting): Flow<List<LibraryManga>> {
    return preparedUncategorized(sort).asRxFlowable(BackpressureStrategy.LATEST).asFlow()
  }

  override fun subscribeToCategory(
    categoryId: Long,
    sort: LibrarySorting
  ): Flow<List<LibraryManga>> {
    return preparedToCategory(categoryId, sort).asRxFlowable(BackpressureStrategy.LATEST).asFlow()
  }

  override fun findAll(sort: LibrarySorting): List<LibraryManga> {
    return preparedAll(sort).asBlocking()
  }

  override fun findUncategorized(sort: LibrarySorting): List<LibraryManga> {
    return preparedUncategorized(sort).asBlocking()
  }

  override fun findForCategory(categoryId: Long, sort: LibrarySorting): List<LibraryManga> {
    return preparedToCategory(categoryId, sort).asBlocking()
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
