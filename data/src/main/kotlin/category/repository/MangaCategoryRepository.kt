/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.category.repository

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import io.reactivex.Completable
import tachiyomi.core.db.asImmediateCompletable
import tachiyomi.core.db.withId
import tachiyomi.core.db.withIds
import tachiyomi.data.category.table.MangaCategoryTable
import tachiyomi.domain.category.model.MangaCategory
import tachiyomi.domain.category.repository.MangaCategoryRepository
import javax.inject.Inject

internal class MangaCategoryRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : MangaCategoryRepository {

  override fun save(mangaCategory: MangaCategory): Completable {
    return storio.put()
      .`object`(mangaCategory)
      .prepare()
      .asImmediateCompletable()
  }

  override fun save(mangaCategories: Collection<MangaCategory>): Completable {
    return storio.put()
      .objects(mangaCategories)
      .prepare()
      .asImmediateCompletable()
  }

  override fun deleteForManga(mangaId: Long): Completable {
    return storio.delete()
      .withId(MangaCategoryTable.TABLE, MangaCategoryTable.COL_MANGA_ID, mangaId)
      .prepare()
      .asImmediateCompletable()
  }

  override fun deleteForMangas(mangaIds: Collection<Long>): Completable {
    return storio.delete()
      .withIds(MangaCategoryTable.TABLE, MangaCategoryTable.COL_MANGA_ID, mangaIds)
      .prepare()
      .asImmediateCompletable()
  }

}
