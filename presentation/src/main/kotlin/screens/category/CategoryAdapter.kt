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
import tachiyomi.domain.category.model.Category
import tachiyomi.ui.adapter.BaseListAdapter
import tachiyomi.ui.adapter.ItemCallback

class CategoryAdapter(
  private val listener: Listener
) : BaseListAdapter<Category, CategoryHolder>() {

  private var selectedIds = emptySet<Long>()
  private var nowSelectedIds = emptySet<Long>()

  private var dragging: Category? = null

  private val touchHelper = ItemTouchHelper(TouchHelperCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
    return CategoryHolder(parent, this)
  }

  override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
    error("Unused")
  }

  override fun onBindViewHolder(holder: CategoryHolder, position: Int, payloads: List<Any>) {
    val category = getItem(position)

    if (payloads.isEmpty()) {
      val isSelected = category.id in nowSelectedIds
      holder.bind(category, isSelected)
    } else {
      val payload = payloads.first { it is Payload } as Payload
      if (payload.nameChanged) {
        holder.bindName(category)
      }
      if (payload.selectionChanged) {
        val isSelected = category.id in nowSelectedIds
        holder.bindIsSelected(isSelected)
      }
    }
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    touchHelper.attachToRecyclerView(recyclerView)
  }

  fun submitCategories(categories: List<Category>, selectedCategories: Set<Long>) {
    selectedIds = selectedCategories
    submitList(categories, forceSubmit = true)
  }

  override fun onListUpdated() {
    nowSelectedIds = selectedIds
  }

  override fun getDiffCallback(
    oldList: List<Category>,
    newList: List<Category>
  ): DiffUtil.Callback {
    return DiffCallback(oldList, newList, nowSelectedIds, selectedIds)
  }

  fun handleClick(position: Int) {
    val category = getItemOrNull(position) ?: return
    if (nowSelectedIds.isNotEmpty()) {
      listener.onCategoryClick(category)
    }
  }

  fun handleLongClick(position: Int) {
    val category = getItemOrNull(position) ?: return
    listener.onCategoryLongClick(category)
  }

  fun handleReorderTouchDown(holder: CategoryHolder) {
    touchHelper.startDrag(holder)
  }

  private class DiffCallback(
    oldList: List<Category>,
    newList: List<Category>,
    val oldSelected: Set<Long>,
    val newSelected: Set<Long>
  ) : ItemCallback<Category>(oldList, newList) {

    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
      return oldItem.name == newItem.name && !selectionChanged(newItem)
    }

    override fun getChangePayload(oldItem: Category, newItem: Category): Any? {
      return Payload(
        nameChanged = oldItem.name != newItem.name,
        selectionChanged = selectionChanged(newItem)
      )
    }

    private fun selectionChanged(category: Category): Boolean {
      return category.id in oldSelected != category.id in newSelected
    }
  }

  inner class TouchHelperCallback : ItemTouchHelper.SimpleCallback(
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
        viewHolder.itemView.isPressed = true
      }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
      super.clearView(recyclerView, viewHolder)

      val dragList = dragList
      if (dragList != null && dragging != null && dragTo != -1) {
        val categoryMoved = dragList[dragTo]
        listener.reorderCategory(categoryMoved, dragTo)
      }

      viewHolder.itemView.isPressed = false

      this.dragList = null
      dragging = null
      dragTo = -1
    }

    override fun isLongPressDragEnabled(): Boolean {
      return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
      return false
    }
  }

  private data class Payload(
    val nameChanged: Boolean,
    val selectionChanged: Boolean
  )

  interface Listener {
    fun reorderCategory(category: Category, newPosition: Int)
    fun onCategoryClick(category: Category)
    fun onCategoryLongClick(category: Category)
  }

}
