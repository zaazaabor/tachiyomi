/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import io.reactivex.Single
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

class GetOrAddMangaFromSource @Inject internal constructor(
  private val mangaRepository: MangaRepository
) {

  fun interact(manga: MangaInfo, sourceId: Long): Single<Manga> {
    return mangaRepository.find(manga.key, sourceId)
      .switchIfEmpty(Single.defer {
        val newManga = Manga(
          id = -1,
          sourceId = sourceId,
          key = manga.key,
          title = manga.title,
          artist = manga.artist,
          author = manga.author,
          description = manga.description,
          genres = manga.genres,
          status = manga.status,
          cover = manga.cover
        )
        mangaRepository.save(newManga)
      })
  }

}
