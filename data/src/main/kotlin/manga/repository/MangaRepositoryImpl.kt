/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga.repository

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.Query
import io.reactivex.BackpressureStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tachiyomi.core.db.asBlocking
import tachiyomi.core.rx.asFlow
import tachiyomi.data.manga.sql.MangaTable
import tachiyomi.data.manga.sql.MangaUpdatePutResolver
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject

internal class MangaRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : MangaRepository {

  override fun subscribe(mangaId: Long): Flow<Manga?> {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_ID} = ?")
        .whereArgs(mangaId)
        .build())
      .prepare()
      .asRxFlowable(BackpressureStrategy.LATEST)
      .distinctUntilChanged()
      .asFlow()
      .map { it.orNull() }
  }

  override fun subscribe(key: String, sourceId: Long): Flow<Manga?> {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_KEY} = ? AND ${MangaTable.COL_SOURCE} = ?")
        .whereArgs(key, sourceId)
        .build())
      .prepare()
      .asRxFlowable(BackpressureStrategy.LATEST)
      .distinctUntilChanged()
      .asFlow()
      .map { it.orNull() }
  }

  override fun find(mangaId: Long): Manga? {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_ID} = ?")
        .whereArgs(mangaId)
        .build())
      .prepare()
      .asBlocking()
  }

  override fun find(key: String, sourceId: Long): Manga? {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_KEY} = ? AND ${MangaTable.COL_SOURCE} = ?")
        .whereArgs(key, sourceId)
        .build())
      .prepare()
      .asBlocking()
  }

  override fun save(manga: Manga): Long? {
    return storio.put()
      .`object`(manga)
      .prepare()
      .asBlocking()
      ?.insertedId()
  }

  override fun savePartial(update: MangaUpdate) {
    storio.put()
      .`object`(update)
      .withPutResolver(MangaUpdatePutResolver)
      .prepare()
      .asBlocking()
  }

  override fun deleteNonFavorite() {
    storio.delete()
      .byQuery(DeleteQuery.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_FAVORITE} = ?")
        .whereArgs(0)
        .build())
      .prepare()
      .asBlocking()
  }

}
