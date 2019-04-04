/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.repository

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import tachiyomi.core.db.asBlocking
import tachiyomi.core.db.withId
import tachiyomi.core.db.withIds
import tachiyomi.data.library.sql.MangaCategoryTable
import tachiyomi.domain.library.model.MangaCategory
import tachiyomi.domain.library.repository.MangaCategoryRepository
import javax.inject.Inject

internal class MangaCategoryRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : MangaCategoryRepository {

  override fun save(mangaCategory: MangaCategory) {
    storio.put()
      .`object`(mangaCategory)
      .prepare()
      .asBlocking()
  }

  override fun save(mangaCategories: Collection<MangaCategory>) {
    storio.put()
      .objects(mangaCategories)
      .prepare()
      .asBlocking()
  }

  override fun deleteForManga(mangaId: Long) {
    storio.delete()
      .withId(MangaCategoryTable.TABLE, MangaCategoryTable.COL_MANGA_ID, mangaId)
      .prepare()
      .asBlocking()
  }

  override fun deleteForMangas(mangaIds: Collection<Long>) {
    storio.delete()
      .withIds(MangaCategoryTable.TABLE, MangaCategoryTable.COL_MANGA_ID, mangaIds)
      .prepare()
      .asBlocking()
  }

}
