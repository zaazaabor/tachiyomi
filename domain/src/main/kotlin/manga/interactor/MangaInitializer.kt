/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import io.reactivex.Maybe
import tachiyomi.core.stdlib.Optional
import tachiyomi.domain.library.repository.LibraryCovers
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.Source
import tachiyomi.source.model.MangaInfo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MangaInitializer @Inject internal constructor(
  private val mangaRepository: MangaRepository,
  private val sourceManager: SourceManager,
  private val libraryCovers: LibraryCovers
) {

  // TODO error handling
  fun interact(source: Source, manga: Manga, force: Boolean = false) = Maybe.defer {
    if (!force && lastInitBelowMinInterval(manga)) return@defer Maybe.empty<Manga>()

    val now = System.currentTimeMillis()

    val infoQuery = MangaInfo(
      key = manga.key,
      title = manga.title,
      artist = manga.artist,
      author = manga.author,
      description = manga.description,
      genres = manga.genres,
      status = manga.status,
      cover = manga.cover
    )
    val newInfo = source.fetchMangaDetails(infoQuery)

    val update = MangaUpdate(
      id = manga.id,
      key = if (newInfo.key.isEmpty() || newInfo.key == manga.key) {
        Optional.None
      } else {
        Optional.of(newInfo.key)
      },
      title = if (newInfo.title.isEmpty() || newInfo.title == manga.title) {
        Optional.None
      } else {
        Optional.of(newInfo.title)
      },
      artist = Optional.of(newInfo.artist),
      author = Optional.of(newInfo.author),
      description = Optional.of(newInfo.description),
      genres = Optional.of(newInfo.genres),
      status = Optional.of(newInfo.status),
      cover = Optional.of(newInfo.cover),
      lastInit = Optional.of(now)
    )

    val updatedManga = manga.copy(
      key = if (newInfo.key.isEmpty()) manga.key else newInfo.key,
      title = if (newInfo.title.isEmpty()) manga.title else newInfo.title,
      artist = newInfo.artist,
      author = newInfo.author,
      description = newInfo.description,
      genres = newInfo.genres,
      status = newInfo.status,
      cover = newInfo.cover,
      lastInit = now
    )

    mangaRepository.savePartial(update)
      .andThen(Maybe.just(updatedManga))
      .doOnSuccess { libraryCovers.find(manga.id).setLastModified(now) }
  }

  fun interact(manga: Manga, force: Boolean = false): Maybe<Manga> {
    val source = sourceManager.get(manga.sourceId) ?: return Maybe.empty()
    return interact(source, manga, force)
  }

  private fun lastInitBelowMinInterval(manga: Manga): Boolean {
    return System.currentTimeMillis() - manga.lastInit < INIT_MIN_INTERVAL
  }

  private companion object {
    val INIT_MIN_INTERVAL = TimeUnit.DAYS.toMillis(30)
  }

}
