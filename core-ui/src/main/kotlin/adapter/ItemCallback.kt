/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.adapter

import androidx.recyclerview.widget.DiffUtil

abstract class ItemCallback<T>(
  val oldList: List<T>,
  val newList: List<T>
) : DiffUtil.Callback() {

  override fun getOldListSize(): Int {
    return oldList.size
  }

  override fun getNewListSize(): Int {
    return newList.size
  }

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val oldItem = oldList[oldItemPosition]
    val newItem = newList[newItemPosition]
    if (oldItem != null && newItem != null) {
      return areItemsTheSame(oldItem, newItem)
    }
    // If both items are null we consider them the same.
    return oldItem == null && newItem == null
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    val oldItem = oldList[oldItemPosition]
    val newItem = newList[newItemPosition]
    if (oldItem != null && newItem != null) {
      return areContentsTheSame(oldItem, newItem)
    }
    if (oldItem == null && newItem == null) {
      return true
    }
    // There is an implementation bug if we reach this point. Per the docs, this
    // method should only be invoked when areItemsTheSame returns true. That
    // only occurs when both items are non-null or both are null and both of
    // those cases are handled above.
    throw AssertionError()
  }

  override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
    val oldItem = oldList[oldItemPosition]
    val newItem = newList[newItemPosition]
    if (oldItem != null && newItem != null) {
      return getChangePayload(oldItem, newItem)
    }
    // There is an implementation bug if we reach this point. Per the docs, this
    // method should only be invoked when areItemsTheSame returns true AND
    // areContentsTheSame returns false. That only occurs when both items are
    // non-null which is the only case handled above.
    throw AssertionError()
  }

  abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

  abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean

  open fun getChangePayload(oldItem: T, newItem: T): Any? {
    return null
  }

}
