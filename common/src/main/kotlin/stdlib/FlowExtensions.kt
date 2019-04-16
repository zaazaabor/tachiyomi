/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.stdlib

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

// TODO rough implementation until a proper debounce is implemented
fun <T> Flow<T>.debounce(time: Long) = flow {
  coroutineScope {
    var idx = 0

    collect { value ->
      val id = ++idx

      delay(time)
      if (id == idx) {
        emit(value)
      }
    }
  }
}
