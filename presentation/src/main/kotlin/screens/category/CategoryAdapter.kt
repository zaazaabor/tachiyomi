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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import tachiyomi.domain.category.Category
import tachiyomi.ui.adapter.BaseListAdapter
import tachiyomi.ui.adapter.ItemCallback

class CategoryAdapter(
  private val listener: Listener
) : BaseListAdapter<Category, CategoryHolder>() {

  private var selectedCategoryIds = emptySet<Long>()

  private var dragging: Category? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
    return CategoryHolder(parent)
  }

  override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
    error("Unused")
  }

  override fun onBindViewHolder(holder: CategoryHolder, position: Int, payloads: List<Any>) {
    val category = getItem(position)

    if (payloads.isEmpty()) {
      val isSelected = category.id in selectedCategoryIds //|| category.id == newDragging?.id
      holder.bind(category, isSelected)
    } else {
      val payload = payloads.first { it is Payload } as Payload
      if (payload.nameChanged) {
        holder.bindName(category.name)
      }
      if (payload.selectionChanged) {
        val isSelected = category.id in selectedCategoryIds //|| category.id == newDragging?.id
        holder.bindIsSelected(isSelected)
      }
    }
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    touchHelper.attachToRecyclerView(recyclerView)
  }

  override fun getDiffCallback(
    oldList: List<Category>,
    newList: List<Category>
  ): DiffUtil.Callback {
    return Callback(oldList, newList /*, dragging, newDragging*/)
  }

  private val touchHelperCallback = object : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
  ) {

    private var dragList: MutableList<Category>? = null
    private var dragTo = -1

    override fun onMove(
      recyclerView: RecyclerView,
      viewHolder: RecyclerView.ViewHolder,
      target: RecyclerView.ViewHolder
    ): Boolean {

      val fromPosition = viewHolder.adapterPosition
      val toPosition = target.adapterPosition

      if (dragList == null) {
        dragList = currentList.toMutableList()
        dragging = currentList[fromPosition]
      }
      val dragList = dragList!!

      if (dragTo != toPosition) {
        dragList.add(toPosition, dragList.removeAt(fromPosition))
        submitList(dragList.toList())
      }

      dragTo = toPosition
      return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
      // unused
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
      super.onSelectedChanged(viewHolder, actionState)

      if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
        viewHolder.itemView.isActivated = true
      }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
      super.clearView(recyclerView, viewHolder)

      val dragList = dragList
      if (dragList != null && dragging != null && dragTo != -1) {
        val categoryMoved = dragList[dragTo]
        listener.onCategoryMoved(categoryMoved, dragTo)
      }

      viewHolder.itemView.isActivated = false

      this.dragList = null
      dragging = null
      dragTo = -1
    }
  }

  private val touchHelper = ItemTouchHelper(touchHelperCallback)

  private class Callback(
    oldList: List<Category>,
    newList: List<Category>
//    val oldDragging: Category?,
//    val newDragging: Category?
  ) : ItemCallback<Category>(oldList, newList) {

    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
      return oldItem.name == newItem.name //&& !draggingChanged(newItem)
    }

    override fun getChangePayload(oldItem: Category, newItem: Category): Any? {
      return Payload(
        nameChanged = oldItem.name != newItem.name,
        selectionChanged = false //draggingChanged(newItem)
      )
    }

//    private fun draggingChanged(category: Category): Boolean {
//      return newDragging == null && oldDragging?.id == category.id ||
//        oldDragging == null && newDragging?.id == category.id
//    }
  }

  private data class Payload(
    val nameChanged: Boolean,
    val selectionChanged: Boolean
  )

  interface Listener {
    fun onCategoryMoved(category: Category, newPosition: Int)
  }

}
