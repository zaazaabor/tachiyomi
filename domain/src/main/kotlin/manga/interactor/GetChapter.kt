/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.manga.interactor

import io.reactivex.Maybe
import kotlinx.coroutines.withContext
import tachiyomi.core.rx.CoroutineDispatchers
import tachiyomi.domain.manga.model.Chapter
import tachiyomi.domain.manga.repository.ChapterRepository
import javax.inject.Inject

class GetChapter @Inject constructor(
  private val repository: ChapterRepository,
  private val dispatchers: CoroutineDispatchers
) {

  suspend fun await(id: Long) = withContext(dispatchers.io) {
    repository.find(id)
  }

  suspend fun await(key: String, mangaId: Long) = withContext(dispatchers.io) {
    repository.find(key, mangaId)
  }

  fun interact(key: String, mangaId: Long): Maybe<Chapter> {
    return Maybe.fromCallable { repository.find(key, mangaId) }
  }

}
