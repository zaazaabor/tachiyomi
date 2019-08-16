/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Coroutines dispatchers available to the app.
 */
interface CoroutineDispatchers {

  val io: CoroutineDispatcher

  val computation: CoroutineDispatcher

  val single: CoroutineDispatcher // TODO not sure this will be used

  val main: CoroutineDispatcher

}
