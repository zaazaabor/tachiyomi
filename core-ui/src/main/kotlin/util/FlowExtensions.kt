/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.scanWithPrevious(): Flow<Pair<T, T?>> = flow {
  var lastValue: T? = null
  collect { value ->
    val newPair = Pair(value, lastValue)
    lastValue = value
    emit(newPair)
  }
}
