/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.library_controller.*
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.R
import tachiyomi.ui.controller.MvpController
import tachiyomi.ui.controller.withHorizontalTransition
import tachiyomi.ui.glide.GlideController
import tachiyomi.ui.glide.GlideProvider
import tachiyomi.ui.category.CategoryController
import tachiyomi.ui.home.HomeChildController
import tachiyomi.ui.manga.MangaController
import tachiyomi.ui.widget.CustomViewTabLayout

class LibraryController : MvpController<LibraryPresenter>(),
  HomeChildController,
  GlideController,
  LibraryAdapter.Listener,
  LibraryChangeCategoriesDialog.Listener {

  private var adapter: LibraryCategoryAdapter? = null

  override val glideProvider = GlideProvider.from(this)

  private var actionMode: ActionMode? = null
  private var actionModeCallback: ActionModeCallback? = null

  override fun getPresenterClass() = LibraryPresenter::class.java

  override fun getModule() = LibraryModule(this)

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.library_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    library_swipe_refresh.setOnRefreshListener { onSwipeRefresh() }

    adapter = LibraryCategoryAdapter(glideProvider.get(), this)
    library_recycler.layoutManager = GridLayoutManager(view.context, 2)
    library_recycler.adapter = adapter
//    library_tabs.setupWithViewPager(library_pager)
    library_tabs.setOnSettingsClickListener(::onCategorySettingsClick)
    library_tabs.addOnTabSelectedListener(object : CustomViewTabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: CustomViewTabLayout.Tab) {
        presenter.setSelectedCategory(tab.position)
      }

      override fun onTabUnselected(tab: CustomViewTabLayout.Tab) {}

      override fun onTabReselected(tab: CustomViewTabLayout.Tab) {}

    })
    //library_tabs.setOnChipClickListener(::onChipClick)

    presenter.state
      .scanWithPrevious()
      .subscribeWithView { (state, prevState) -> render(state, prevState) }
  }

  override fun onDestroyView(view: View) {
    actionMode?.finish()
    adapter = null
    super.onDestroyView(view)
  }

  private fun render(state: ViewState, prevState: ViewState?) {
    if (state.categories !== prevState?.categories ||
      state.selectedCategoryId != prevState.selectedCategoryId
    ) {
      renderCategories(state.categories, state.selectedCategoryId)
    }
    if (state.library !== prevState?.library || state.selectedManga !== prevState.selectedManga) {
      renderLibrary(state.library, state.selectedManga)
    }
    if (state.selectedManga !== prevState?.selectedManga) {
      renderSelectedManga(state.selectedManga)
    }
    renderUpdatingCategory(state.showUpdatingCategory) // Changes internally checked
  }

  private fun renderCategories(categories: List<Category>, selectedCategoryId: Long?) {
    library_tabs.setCategories(categories, selectedCategoryId)
  }

  private fun renderLibrary(library: List<LibraryManga>, selectedManga: Set<Long>) {
    adapter?.submitManga(library, selectedManga)
  }

  private fun renderSelectedManga(selectedManga: Set<Long>) {
    if (selectedManga.isEmpty()) {
      actionMode?.finish()
      return
    }

    if (actionMode == null) {
      val callback = ActionModeCallback()
      actionModeCallback = callback
      actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(callback)
    }

    actionModeCallback?.render(selectedManga, actionMode)
  }

  private fun renderUpdatingCategory(showUpdatingCategory: Boolean) {
    library_swipe_refresh.isRefreshing = showUpdatingCategory
  }

  override fun onMangaClick(manga: LibraryManga) {
    if (actionMode == null) {
      findRootRouter().pushController(MangaController(manga.id).withHorizontalTransition())
    } else {
      onMangaLongClick(manga)
    }
  }

  override fun onMangaLongClick(manga: LibraryManga) {
    presenter.toggleMangaSelection(manga)
  }

  private fun onSwipeRefresh() {
    presenter.updateSelectedCategory()
  }

  private fun onCategorySettingsClick() {
    router.pushController(CategoryController().withHorizontalTransition())
  }

  private fun showSetCategoriesDialog(selectedManga: Set<Long>) {
    val categories = presenter.getCategories().takeUnless { it.isEmpty() } ?: return
    val preselected = emptyArray<Int>() // TODO
    LibraryChangeCategoriesDialog(this, selectedManga, categories, preselected).showDialog(router)
  }

  override fun updateCategoriesForMangas(
    categoryIds: Collection<Long>,
    mangaIds: Collection<Long>
  ) {
    presenter.setCategoriesForMangas(categoryIds, mangaIds)
  }

  private inner class ActionModeCallback : ActionMode.Callback {

    private var selectedManga: Set<Long> = emptySet()

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
      mode.menuInflater.inflate(R.menu.library_menu, menu)
      return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
      mode.title = resources?.getString(R.string.label_selected, selectedManga.size)
      return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
      when (item.itemId) {
        R.id.action_set_categories -> {
          showSetCategoriesDialog(selectedManga)
        }
      }
      return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
      presenter.unselectMangas()
      actionModeCallback = null
      actionMode = null
    }

    fun render(selectedManga: Set<Long>, mode: ActionMode?) {
      this.selectedManga = selectedManga
      mode?.invalidate()
    }

  }

}
