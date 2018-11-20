/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import io.reactivex.Maybe
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.Source
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

class MangaInitializer @Inject internal constructor(
  private val mangaRepository: MangaRepository,
  private val sourceManager: SourceManager
) {

  fun interact(source: Source, manga: Manga, force: Boolean = false): Maybe<Manga> {
    if (!force && manga.initialized) return Maybe.empty()

    val stubManga = MangaInfo(
      key = manga.key,
      title = manga.title,
      artist = manga.artist,
      author = manga.author,
      description = manga.description,
      genres = manga.genres,
      status = manga.status,
      cover = manga.cover,
      initialized = manga.initialized
    )
    return Maybe.fromCallable { source.fetchMangaDetails(stubManga) }
      .flatMap { sourceManga ->
        val updatedManga = manga.copy(
          key = if (sourceManga.key.isEmpty()) manga.key else sourceManga.key,
          title = if (sourceManga.title.isEmpty()) manga.title else sourceManga.title,
          artist = sourceManga.artist,
          author = sourceManga.author,
          description = sourceManga.description,
          genres = sourceManga.genres,
          status = sourceManga.status,
          cover = sourceManga.cover,
          initialized = true
        )
        mangaRepository.updateMangaDetails(updatedManga)
          .andThen(Maybe.just(updatedManga))
      }
  }

  fun interact(manga: Manga, force: Boolean = false): Maybe<Manga> {
    val source = sourceManager.get(manga.source) ?: return Maybe.empty()
    return interact(source, manga, force)
  }

}
