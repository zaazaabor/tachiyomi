/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.category.repository

import io.reactivex.Completable
import io.reactivex.Observable
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.model.CategoryUpdate
import tachiyomi.domain.category.model.CategoryWithCount

interface CategoryRepository {

  fun subscribe(): Observable<List<Category>>

  fun subscribeWithCount(): Observable<List<CategoryWithCount>>

  fun subscribeForManga(mangaId: Long): Observable<List<Category>>

  fun save(category: Category): Completable

  fun save(categories: Collection<Category>): Completable

  fun savePartial(update: CategoryUpdate): Completable

  fun savePartial(updates: Collection<CategoryUpdate>): Completable

  fun delete(categoryId: Long): Completable

  fun delete(categoryIds: Collection<Long>): Completable

}
