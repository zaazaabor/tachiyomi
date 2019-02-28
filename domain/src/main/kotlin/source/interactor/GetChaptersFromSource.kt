/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.source.interactor

import io.reactivex.Single
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.Source
import tachiyomi.source.model.ChapterInfo
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

class GetChaptersFromSource @Inject constructor() {

  fun interact(source: Source, manga: Manga): Single<List<ChapterInfo>> {
    return Single.fromCallable {
      val mangaInfo = MangaInfo(
        manga.key,
        manga.title,
        manga.artist,
        manga.author,
        manga.description,
        manga.genres,
        manga.status,
        manga.cover
      )
      source.fetchChapterList(mangaInfo)
    }
  }

}
