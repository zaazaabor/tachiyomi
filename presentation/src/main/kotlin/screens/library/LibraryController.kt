/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import kotlinx.android.synthetic.main.library_controller.*
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.library.model.Library
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.R
import tachiyomi.ui.controller.MvpController
import tachiyomi.ui.controller.withHorizontalTransition
import tachiyomi.ui.glide.GlideController
import tachiyomi.ui.glide.GlideProvider
import tachiyomi.ui.screens.category.CategoryController
import tachiyomi.ui.screens.home.HomeChildController
import tachiyomi.ui.screens.manga.MangaController

class LibraryController : MvpController<LibraryPresenter>(),
  HomeChildController,
  GlideController,
  LibraryAdapter.Listener {

  private var adapter: LibraryAdapter? = null

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

    adapter = LibraryAdapter(this, glideProvider.get())
    library_pager.adapter = adapter
    library_tabs.setupWithViewPager(library_pager)
    library_tabs.setOnSettingsClickListener(::onCategorySettingsClick)

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
    if (state.library !== prevState?.library || state.selectedManga !== prevState.selectedManga) {
      renderLibrary(state.library, state.selectedManga)
    }
    if (state.selectedManga !== prevState?.selectedManga) {
      renderSelectedManga(state.selectedManga)
    }
  }

  private fun renderLibrary(library: Library, selectedManga: Set<Long>) {
    adapter?.setItems(library, selectedManga)
    library_tabs.submitList(library)
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

  override fun onMangaClick(manga: LibraryManga) {
    if (actionMode == null) {
      findRootRouter().pushController(MangaController(manga.mangaId).withHorizontalTransition())
    } else {
      onMangaLongClick(manga)
    }
  }

  override fun onMangaLongClick(manga: LibraryManga) {
    presenter.toggleMangaSelection(manga)
  }

  private fun onCategorySettingsClick() {
    router.pushController(CategoryController().withHorizontalTransition())
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
