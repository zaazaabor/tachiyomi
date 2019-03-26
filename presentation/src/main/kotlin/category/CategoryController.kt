/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.category

import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.category_controller.*
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.category.model.Category
import tachiyomi.ui.R
import tachiyomi.ui.controller.MvpController
import tachiyomi.ui.home.HomeChildController

class CategoryController : MvpController<CategoryPresenter>(),
  HomeChildController,
  HomeChildController.FAB,
  CategoryAdapter.Listener,
  CategoryCreateDialog.Listener,
  CategoryRenameDialog.Listener {

  private var adapter: CategoryAdapter? = null

  private var actionMode: ActionMode? = null
  private var actionModeCallback: ActionModeCallback? = null

  override fun getPresenterClass() = CategoryPresenter::class.java

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.category_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    setupToolbarNavWithHomeController(category_toolbar)

    adapter = CategoryAdapter(this)
    category_recycler.adapter = adapter

    presenter.stateObserver
      .scanWithPrevious()
      .subscribeWithView { (state, prevState) -> render(state, prevState) }
  }

  override fun onDestroyView(view: View) {
    actionMode?.finish()
    actionMode = null
    adapter = null
    super.onDestroyView(view)
  }

  override fun createFAB(container: ViewGroup): FloatingActionButton {
    val inflater = LayoutInflater.from(container.context)
    val fab = inflater.inflate(R.layout.category_fab, container, false)
    fab.setOnClickListener { showCreateCategoryDialog() }
    return fab as FloatingActionButton
  }

  private fun render(state: ViewState, prevState: ViewState?) {
    if (state.categories !== prevState?.categories
      || state.selectedCategories !== prevState.selectedCategories) {

      renderCategories(state.categories, state.selectedCategories)
    }
    if (state.selectedCategories !== prevState?.selectedCategories) {
      renderSelectedCategories(state.selectedCategories)
    }
  }

  private fun renderCategories(categories: List<Category>, selectedCategories: Set<Long>) {
    adapter?.submitCategories(categories, selectedCategories)
  }

  private fun renderSelectedCategories(selectedCategories: Set<Long>) {
    if (selectedCategories.isEmpty()) {
      if (actionMode != null) {
        actionMode?.finish()
        actionMode = null
        actionModeCallback = null
      }
      return
    }

    if (actionMode == null) {
      actionModeCallback = ActionModeCallback()
      actionMode = activity?.startActionMode(actionModeCallback)
    }

    actionModeCallback?.render(selectedCategories, actionMode)
  }

  private fun showCreateCategoryDialog() {
    CategoryCreateDialog(this).showDialog(router)
  }

  private fun showRenameCategoryDialog(categoryId: Long) {
    val category = presenter.getCategory(categoryId) ?: return
    CategoryRenameDialog(this, category).showDialog(router)
  }

  private fun showDeleteCategories(categories: Set<Long>) {
    val activity = activity ?: return
    MaterialDialog(activity)
      .message(text = "Delete selected categories?")
      .positiveButton(android.R.string.yes) { presenter.deleteCategories(categories) }
      .negativeButton(android.R.string.no)
      .show()
  }

  override fun createCategory(name: String) {
    presenter.createCategory(name)
  }

  override fun renameCategory(categoryId: Long, name: String) {
    presenter.renameCategory(categoryId, name)
  }

  override fun reorderCategory(category: Category, newPosition: Int) {
    presenter.reorderCategory(category, newPosition)
  }

  override fun onCategoryClick(category: Category) {
    if (actionMode != null) {
      presenter.toggleCategorySelection(category)
    }
  }

  override fun onCategoryLongClick(category: Category) {
    presenter.toggleCategorySelection(category)
  }

  private inner class ActionModeCallback : ActionMode.Callback {

    private var selectedCategories: Set<Long> = emptySet()

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
      mode.menuInflater.inflate(R.menu.category_menu, menu)
      return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
      val renameItem = menu.findItem(R.id.action_rename)
      renameItem.isVisible = selectedCategories.size == 1
      mode.title = resources?.getString(R.string.label_selected, selectedCategories.size)
      return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
      when (item.itemId) {
        R.id.action_rename -> {
          val categoryId = selectedCategories.firstOrNull() ?: return true
          showRenameCategoryDialog(categoryId)
        }
        R.id.action_delete -> showDeleteCategories(selectedCategories)
      }
      return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
      presenter.unselectCategories()
    }

    fun render(selectedCategories: Set<Long>, mode: ActionMode?) {
      this.selectedCategories = selectedCategories
      mode?.invalidate()
    }

  }

}
