/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.category

import android.view.MotionEvent
import android.view.ViewGroup
import kotlinx.android.synthetic.main.category_item.*
import tachiyomi.domain.category.Category
import tachiyomi.ui.R
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.util.inflate
import tachiyomi.ui.widget.TextOvalDrawable

class CategoryHolder(
  parent: ViewGroup,
  adapter: CategoryAdapter
) : BaseViewHolder(parent.inflate(R.layout.category_item)) {

  init {
    itemView.setOnClickListener {
      adapter.handleClick(adapterPosition)
    }
    itemView.setOnLongClickListener {
      adapter.handleLongClick(adapterPosition)
      true
    }
    category_reorder.setOnTouchListener { _, event ->
      if (event.actionMasked == MotionEvent.ACTION_DOWN) {
        adapter.handleReorderTouchDown(this)
      }
      true
    }
  }

  fun bind(category: Category, isSelected: Boolean) {
    bindName(category.name)
    bindIsSelected(isSelected)
  }

  fun bindName(name: String) {
    category_text.text = name

    val initial = name.firstOrNull()?.toUpperCase()?.toString() ?: ""
    val drawable = TextOvalDrawable(
      text = initial,
      backgroundColor = TextOvalDrawable.Colors.getColor(initial)
    )

    category_icon.setImageDrawable(drawable)
  }

  fun bindIsSelected(isSelected: Boolean) {
    itemView.isActivated = isSelected
  }

}
