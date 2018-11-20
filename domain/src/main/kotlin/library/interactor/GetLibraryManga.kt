/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import io.reactivex.Flowable
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class GetLibraryManga @Inject constructor(
  private val libraryRepository: LibraryRepository
) {

  fun interact(): Flowable<List<LibraryManga>> {
    return libraryRepository.getLibraryMangas()
  }
}
