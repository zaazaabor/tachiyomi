/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalogs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import tachiyomi.ui.util.dpToPx
import tachiyomi.ui.util.getDrawableAttr

class CatalogDividerDecoration(context: Context) : RecyclerView.ItemDecoration() {

  private val divider = context.getDrawableAttr(android.R.attr.listDivider)

  private val bounds = Rect()

  private val left = 72.dpToPx

  override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    if (parent.layoutManager == null) return

    canvas.save()

    val right = parent.width

    val childCount = parent.childCount
    for (i in 0 until childCount) {
      val child = parent.getChildAt(i)
      if (shouldDrawDivider(child, parent)) {
        parent.getDecoratedBoundsWithMargins(child, bounds)
        val bottom = bounds.bottom + Math.round(child.translationY)
        val top = bottom - divider.intrinsicHeight
        divider.setBounds(left, top, right, bottom)
        divider.draw(canvas)
      }
    }
    canvas.restore()
  }

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    if (shouldDrawDivider(view, parent)) {
      outRect.set(0, 0, 0, divider.intrinsicHeight)
    } else {
      outRect.setEmpty()
    }
  }

  private fun shouldDrawDivider(view: View, parent: RecyclerView): Boolean {
    return parent.getChildViewHolder(view) is CatalogHolder
  }

}
