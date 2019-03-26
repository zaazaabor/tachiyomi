/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import io.reactivex.Maybe
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.repository.ChapterRepository
import javax.inject.Inject

class GetChapter @Inject constructor(
  private val repository: ChapterRepository
) {

  fun interact(id: Long): Maybe<Chapter> {
    return repository.find(id)
      .onErrorComplete()
  }

  fun interact(key: String, sourceId: Long): Maybe<Chapter> {
    return repository.find(key, sourceId)
      .onErrorComplete()
  }

}
