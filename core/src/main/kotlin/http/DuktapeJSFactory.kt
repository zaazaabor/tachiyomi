/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import com.squareup.duktape.Duktape
import javax.inject.Inject

/**
 * A factory for creating instances of [DuktapeJS].
 */
internal class DuktapeJSFactory @Inject constructor() : JSFactory {

  /**
   * Returns a new instance of [DuktapeJS].
   */
  override fun create(): JS {
    return DuktapeJS(Duktape.create())
  }

}
