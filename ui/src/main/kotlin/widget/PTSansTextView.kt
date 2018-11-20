/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import tachiyomi.app.R
import java.util.HashMap

class PTSansTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
  TextView(context, attrs) {

  companion object {
    const val PTSANS_NARROW = 0
    const val PTSANS_NARROW_BOLD = 1

    // Map where typefaces are cached
    private val typefaces = HashMap<Int, Typeface>(2)
  }

  init {
    if (attrs != null) {
      val values = context.obtainStyledAttributes(attrs, R.styleable.PTSansTextView)

      val typeface = values.getInt(R.styleable.PTSansTextView_typeface, 0)

      setTypeface(typefaces.getOrPut(typeface) {
        Typeface.createFromAsset(context.assets, when (typeface) {
          PTSANS_NARROW -> "fonts/PTSans-Narrow.ttf"
          PTSANS_NARROW_BOLD -> "fonts/PTSans-NarrowBold.ttf"
          else -> throw IllegalArgumentException("Font not found $typeface")
        })
      })

      values.recycle()
    }
  }

  override fun onDraw(canvas: Canvas) {
    // Draw two times for a more visible shadow around the text
    super.onDraw(canvas)
    super.onDraw(canvas)
  }

}
