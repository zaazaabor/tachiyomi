/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.util

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

fun View.clicks(): Flow<Unit> = callbackFlow<Unit> {
  val listener = View.OnClickListener {
    if (!isClosedForSend) {
      offer(Unit)
    }
  }
  setOnClickListener(listener)
  awaitClose { setOnClickListener(null) }
}.conflate()

fun Toolbar.itemClicks(): Flow<MenuItem> = callbackFlow<MenuItem> {
  val listener = Toolbar.OnMenuItemClickListener {
    if (!isClosedForSend) {
      offer(it)
    }
    true
  }
  setOnMenuItemClickListener(listener)
  awaitClose { setOnMenuItemClickListener(null) }
}.conflate()

fun Toolbar.navigationClicks(): Flow<Unit> = callbackFlow<Unit> {
  val listener = View.OnClickListener {
    if (!isClosedForSend) {
      offer(Unit)
    }
  }
  setNavigationOnClickListener(listener)
  awaitClose { setNavigationOnClickListener(null) }
}.conflate()
