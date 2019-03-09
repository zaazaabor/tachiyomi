/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.category.repository

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.queries.Query
import com.pushtorefresh.storio3.sqlite.queries.RawQuery
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Observable
import tachiyomi.core.db.asImmediateCompletable
import tachiyomi.core.db.withId
import tachiyomi.core.db.withIds
import tachiyomi.data.category.sql.CategoryTable
import tachiyomi.data.category.sql.CategoryUpdatePutResolver
import tachiyomi.data.category.sql.CategoryWithCountGetResolver
import tachiyomi.data.category.sql.MangaCategoryTable
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.model.CategoryUpdate
import tachiyomi.domain.category.model.CategoryWithCount
import tachiyomi.domain.category.repository.CategoryRepository
import javax.inject.Inject

internal class CategoryRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : CategoryRepository {

  private val categories = storio.get()
    .listOfObjects(Category::class.java)
    .withQuery(Query.builder()
      .table(CategoryTable.TABLE)
      .orderBy(CategoryTable.COL_ORDER)
      .build())
    .prepare()
    .asRxFlowable(BackpressureStrategy.LATEST)
    .toObservable()
    .replay(1)
    .autoConnect()

  override fun subscribe(): Observable<List<Category>> {
    return categories
  }

  override fun subscribeWithCount(): Observable<List<CategoryWithCount>> {
    return storio.get()
      .listOfObjects(CategoryWithCount::class.java)
      .withQuery(RawQuery.builder()
        .query(CategoryWithCountGetResolver.query)
        .observesTables(CategoryTable.TABLE, MangaCategoryTable.TABLE)
        .build())
      .withGetResolver(CategoryWithCountGetResolver)
      .prepare()
      .asRxFlowable(BackpressureStrategy.LATEST)
      .toObservable()
  }

  override fun subscribeForManga(mangaId: Long): Observable<List<Category>> {
    return storio.get()
      .listOfObjects(Category::class.java)
      .withQuery(RawQuery.builder()
        .query("""SELECT ${CategoryTable.TABLE}.*
          FROM ${CategoryTable.TABLE}
          JOIN ${MangaCategoryTable.TABLE}
          ON ${CategoryTable.COL_ID} = ${MangaCategoryTable.COL_CATEGORY_ID}
          WHERE ${MangaCategoryTable.COL_MANGA_ID} = ?""")
        .args(mangaId)
        .build())
      .prepare()
      .asRxFlowable(BackpressureStrategy.LATEST)
      .toObservable()
  }

  override fun save(category: Category): Completable {
    return storio.put()
      .`object`(category)
      .prepare()
      .asImmediateCompletable()
  }

  override fun save(categories: Collection<Category>): Completable {
    return storio.put()
      .objects(categories)
      .prepare()
      .asImmediateCompletable()
  }

  override fun savePartial(update: CategoryUpdate): Completable {
    return storio.put()
      .`object`(update)
      .withPutResolver(CategoryUpdatePutResolver)
      .prepare()
      .asImmediateCompletable()
  }

  override fun savePartial(updates: Collection<CategoryUpdate>): Completable {
    return storio.put()
      .objects(updates)
      .withPutResolver(CategoryUpdatePutResolver)
      .prepare()
      .asImmediateCompletable()
  }

  override fun delete(categoryId: Long): Completable {
    return storio.delete()
      .withId(CategoryTable.TABLE, CategoryTable.COL_ID, categoryId)
      .prepare()
      .asImmediateCompletable()
  }

  override fun delete(categoryIds: Collection<Long>): Completable {
    return storio.delete()
      .withIds(CategoryTable.TABLE, CategoryTable.COL_ID, categoryIds)
      .prepare()
      .asImmediateCompletable()
  }

}
