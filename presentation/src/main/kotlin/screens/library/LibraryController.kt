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
import android.view.View
import android.view.ViewGroup
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
    adapter = null
    super.onDestroyView(view)
  }

  private fun render(state: ViewState, prevState: ViewState?) {
    if (state.library !== prevState?.library) {
      renderLibrary(state.library)
    }
  }

  private fun renderLibrary(library: Library) {
    adapter?.setItems(library)
    library_tabs.submitList(library)
  }

  override fun onMangaClick(manga: LibraryManga) {
    findRootRouter().pushController(MangaController(manga.mangaId).withHorizontalTransition())
  }

  private fun onCategorySettingsClick() {
    router.pushController(CategoryController().withHorizontalTransition())
  }

}
