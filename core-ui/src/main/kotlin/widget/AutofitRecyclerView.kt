/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AutofitRecyclerView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

  private var columnWidth = -1

  var spanCount = 0
    set(value) {
      field = value
      if (value > 0) {
        (layoutManager as? GridLayoutManager)?.spanCount = value
      }
    }

  init {
    if (attrs != null) {
      val attrsArray = intArrayOf(android.R.attr.columnWidth)
      val array = context.obtainStyledAttributes(attrs, attrsArray)
      columnWidth = array.getDimensionPixelSize(0, -1)
      array.recycle()
    }
  }

  override fun onMeasure(widthSpec: Int, heightSpec: Int) {
    super.onMeasure(widthSpec, heightSpec)
    if (spanCount == 0 && columnWidth > 0) {
      val count = Math.max(1, measuredWidth / columnWidth)
      spanCount = count
    }
  }

  override fun setLayoutManager(layout: LayoutManager?) {
    super.setLayoutManager(layout)
    if (layout is GridLayoutManager && spanCount > 0) {
      layout.spanCount = spanCount
    }
  }

}
