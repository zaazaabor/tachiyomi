/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.manga

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.Query
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import tachiyomi.core.db.asImmediateCompletable
import tachiyomi.core.db.toOptional
import tachiyomi.core.stdlib.Optional
import tachiyomi.data.manga.resolver.MangaUpdatePutResolver
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject

internal class MangaRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : MangaRepository {

  override fun subscribe(mangaId: Long): Observable<Optional<Manga>> {
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
      .map { it.toOptional() }
      .toObservable()
  }

  override fun subscribe(key: String, sourceId: Long): Observable<Optional<Manga>> {
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
      .map { it.toOptional() }
      .toObservable()
  }

  override fun find(mangaId: Long): Maybe<Manga> {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_ID} = ?")
        .whereArgs(mangaId)
        .build())
      .prepare()
      .asRxMaybe()
  }

  override fun find(key: String, sourceId: Long): Maybe<Manga> {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_KEY} = ? AND ${MangaTable.COL_SOURCE} = ?")
        .whereArgs(key, sourceId)
        .build())
      .prepare()
      .asRxMaybe()
  }

  override fun save(manga: Manga): Single<Manga> {
    return storio.put()
      .`object`(manga)
      .prepare()
      .asRxSingle()
      .map { manga.copy(id = it.insertedId()!!) }
  }

  override fun savePartial(update: MangaUpdate): Completable {
    return storio.put()
      .`object`(update)
      .withPutResolver(MangaUpdatePutResolver)
      .prepare()
      .asImmediateCompletable()
  }

  override fun deleteNonFavorite(): Completable {
    return storio.delete()
      .byQuery(DeleteQuery.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_FAVORITE} = ?")
        .whereArgs(0)
        .build())
      .prepare()
      .asRxCompletable()
  }

}
