/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.extension.model

sealed class LoadResult {

  class Success(val extension: Extension.Installed) : LoadResult()
  class Untrusted(val extension: Extension.Untrusted) : LoadResult()
  class Error(val message: String? = null) : LoadResult() {
    constructor(exception: Throwable) : this(exception.message)
  }

}
