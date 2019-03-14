/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.repository

import io.reactivex.Observable
import io.reactivex.Single
import tachiyomi.domain.library.model.LibraryManga

interface LibraryRepository {

  fun subscribeAll(): Observable<List<LibraryManga>>

  fun subscribeUncategorized(): Observable<List<LibraryManga>>

  fun subscribeToCategory(categoryId: Long): Observable<List<LibraryManga>>

  fun findAll(): Single<List<LibraryManga>>

  fun findUncategorized(): Single<List<LibraryManga>>

  fun findToCategory(categoryId: Long): Single<List<LibraryManga>>

  fun findFavoriteSourceIds(): Single<List<Long>>

}
