/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.source

import android.app.Application
import tachiyomi.data.BuildConfig
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.Source
import tachiyomi.source.TestSource
import javax.inject.Inject

class SourceManagerImpl @Inject constructor(
  private val context: Application
) : SourceManager {

  private val sources = mutableMapOf<Long, Source>()

  init {
    if (BuildConfig.DEBUG) {
      registerSource(TestSource())
    }
  }

  override fun get(key: Long): Source? {
    return sources[key]
  }

  override fun getSources(): List<Source> {
    return sources.values.toList()
  }

  override fun registerSource(source: Source, overwrite: Boolean) {
    if (overwrite || !sources.containsKey(source.id)) {
      sources[source.id] = source
    }
  }

  override fun unregisterSource(source: Source) {
    sources.remove(source.id)
  }

}
