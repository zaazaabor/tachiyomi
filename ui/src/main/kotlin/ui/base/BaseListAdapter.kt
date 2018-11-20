/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T, VH : RecyclerView.ViewHolder>(
  callback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(callback) {

  fun getItemOrNull(position: Int): T? {
    return if (position < 0 || position > itemCount) {
      null
    } else {
      getItem(position)
    }
  }

}
