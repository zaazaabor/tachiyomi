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
import com.pushtorefresh.storio3.sqlite.queries.Query
import com.pushtorefresh.storio3.sqlite.queries.RawQuery
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import tachiyomi.core.db.asBlocking
import tachiyomi.core.db.withId
import tachiyomi.core.db.withIds
import tachiyomi.core.rx.asFlow
import tachiyomi.data.library.sql.CategoryTable
import tachiyomi.data.library.sql.CategoryUpdatePutResolver
import tachiyomi.data.library.sql.CategoryWithCountGetResolver
import tachiyomi.data.library.sql.MangaCategoryTable
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.CategoryUpdate
import tachiyomi.domain.library.model.CategoryWithCount
import tachiyomi.domain.library.repository.CategoryRepository
import javax.inject.Inject

internal class CategoryRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : CategoryRepository {

  private lateinit var cachedCategories: List<Category>

  private val categories = preparedCategories()
    .asRxFlowable(BackpressureStrategy.LATEST)
    .toObservable()
    .doOnNext { cachedCategories = it }
    .replay(1)
    .autoConnect()

  private fun preparedCategories(): PreparedGetListOfObjects<Category> {
    return storio.get()
      .listOfObjects(Category::class.java)
      .withQuery(Query.builder()
        .table(CategoryTable.TABLE)
        .orderBy(CategoryTable.COL_ORDER)
        .build())
      .prepare()
  }

  override fun subscribeAll(): Flow<MutableList<Category>> {
    return categories.asFlow()
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

  override fun findAll(): List<Category> {
    return if (::cachedCategories.isInitialized) {
      cachedCategories
    } else {
      // TODO test if this is actually ever needed
      preparedCategories().asBlocking()
    }
  }

  override fun find(categoryId: Long): Category? {
    return findAll().find { it.id == categoryId }
  }

  override fun save(category: Category) {
    storio.put()
      .`object`(category)
      .prepare()
      .asBlocking()
  }

  override fun save(categories: Collection<Category>) {
    storio.put()
      .objects(categories)
      .prepare()
      .asBlocking()
  }

  override fun savePartial(update: CategoryUpdate) {
    storio.put()
      .`object`(update)
      .withPutResolver(CategoryUpdatePutResolver)
      .prepare()
      .asBlocking()
  }

  override fun savePartial(updates: Collection<CategoryUpdate>) {
    storio.put()
      .objects(updates)
      .withPutResolver(CategoryUpdatePutResolver)
      .prepare()
      .asBlocking()
  }

  override fun delete(categoryId: Long) {
    storio.delete()
      .withId(CategoryTable.TABLE, CategoryTable.COL_ID, categoryId)
      .prepare()
      .asBlocking()
  }

  override fun delete(categoryIds: Collection<Long>) {
    storio.delete()
      .withIds(CategoryTable.TABLE, CategoryTable.COL_ID, categoryIds)
      .prepare()
      .asBlocking()
  }

}
