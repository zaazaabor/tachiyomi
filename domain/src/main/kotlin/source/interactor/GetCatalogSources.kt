/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.source.interactor

import io.reactivex.Single
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.CatalogSource
import javax.inject.Inject

class GetCatalogSources @Inject constructor(
  private val sourceManager: SourceManager
) {

  fun interact(): Single<List<CatalogSource>> {
    return Single.fromCallable { sourceManager.getSources() }
  }
}
