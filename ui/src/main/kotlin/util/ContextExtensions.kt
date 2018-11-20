/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes

/**
 * Returns the color for the given attribute.
 *
 * @param resource the attribute.
 */
fun Context.getResourceColor(@AttrRes resource: Int): Int {
  val typedArray = obtainStyledAttributes(intArrayOf(resource))
  val attrValue = typedArray.getColor(0, 0)
  typedArray.recycle()
  return attrValue
}

fun Context.getDrawableAttr(@AttrRes resource: Int): Drawable? {
  val typedArray = obtainStyledAttributes(intArrayOf(resource))
  val drawable = typedArray.getDrawable(0)
  typedArray.recycle()
  return drawable
}
