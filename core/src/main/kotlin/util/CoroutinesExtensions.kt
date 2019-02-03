/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun launchUI(block: suspend CoroutineScope.() -> Unit): Job {
  return GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, block)
}

fun launchNow(block: suspend CoroutineScope.() -> Unit): Job {
  return GlobalScope.launch(Dispatchers.Main, CoroutineStart.UNDISPATCHED, block)
}
