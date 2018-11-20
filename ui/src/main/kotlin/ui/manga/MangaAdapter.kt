/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.manga

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tachiyomi.app.R
import tachiyomi.ui.base.BaseListAdapter

class MangaAdapter : BaseListAdapter<Any, RecyclerView.ViewHolder>(Diff()) {

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is MangaHeader -> VIEWTYPE_HEADER
      else -> error("Unknown view type for position $position")
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)

    return when (viewType) {
      VIEWTYPE_HEADER -> {
        val view = inflater.inflate(R.layout.manga_header_item, parent, false)
        MangaHeaderHolder(view)
      }
      else -> error("$viewType is not a valid viewtype")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is MangaHeaderHolder -> holder.bind(getItem(position) as? MangaHeader)
    }
  }

  private class Diff : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
      return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
      return true // TODO
    }
  }

  private companion object {
    const val VIEWTYPE_HEADER = 1
  }

}
