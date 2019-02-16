/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.category

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tachiyomi.domain.category.Category
import tachiyomi.ui.adapter.BaseListAdapter

class CategoryAdapter : BaseListAdapter<Category, CategoryHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
    return CategoryHolder(parent)
  }

  override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
    return holder.bind(getItem(position))
  }

  override val itemCallback = object : DiffUtil.ItemCallback<Category>() {

    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
      return oldItem == newItem
    }

  }


}
