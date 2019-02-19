/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.category_controller.*
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.category.Category
import tachiyomi.ui.R
import tachiyomi.ui.controller.MvpController
import tachiyomi.ui.screens.home.HomeChildController

class CategoryController : MvpController<CategoryPresenter>(),
  HomeChildController,
  HomeChildController.FAB,
  CategoryCreateDialog.Listener,
  CategoryAdapter.Listener {

  private var adapter: CategoryAdapter? = null

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
    if (state.categories !== prevState?.categories) {
      renderCategories(state.categories)
    }
  }

  private fun renderCategories(categories: List<Category>) {
    adapter?.submitList(categories)
  }

  private fun showCreateCategoryDialog() {
    CategoryCreateDialog(this).showDialog(router)
  }

  override fun createCategory(name: String) {
    presenter.createCategory(name)
  }

  override fun onCategoryMoved(category: Category, newPosition: Int) {
    presenter.reorderCategory(category, newPosition)
  }

}
