/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

@file:Suppress("NOTHING_TO_INLINE")

package tachiyomi.ui.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

inline fun View.setVisible() {
  visibility = View.VISIBLE
}

inline fun View.setInvisible() {
  visibility = View.INVISIBLE
}

inline fun View.setGone() {
  visibility = View.GONE
}

inline fun View.visibleIf(block: () -> Boolean) {
  visibility = if (block()) View.VISIBLE else View.GONE
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
  return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}
