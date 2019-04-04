/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import io.reactivex.Single
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject

class FindOrInitChapterFromSource @Inject constructor(
  private val getChapter: GetChapter,
  private val syncChaptersFromSource: SyncChaptersFromSource
) {

  fun interact(
    chapterKey: String,
    manga: Manga
  ): Single<Chapter> {
    return getChapter.interact(chapterKey, manga.id)
      .switchIfEmpty(Single.defer {
        syncChaptersFromSource.interact(manga)
          .flatMap { getChapter.interact(chapterKey, manga.id).toSingle() }
      })
  }

}
