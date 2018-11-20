/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.DeepLinkSource
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

class FindOrInitMangaFromChapterKey @Inject constructor(
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource,
  private val mangaInitializer: MangaInitializer
) {

  fun interact(chapterKey: String, source: DeepLinkSource): Single<Manga> {
    return Single
      .defer {
        val mangaKey = source.findMangaKey(chapterKey)
        if (mangaKey != null) {
          val mangaInfo = MangaInfo(key = mangaKey, title = "")
          getOrAddMangaFromSource.interact(mangaInfo, source.id)
        } else {
          Single.error(Exception("Manga key not found"))
        }
      }
      .flatMap {
        mangaInitializer.interact(source, it).switchIfEmpty(Maybe.just(it)).toSingle()
      }
  }

}
