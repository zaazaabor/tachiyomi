/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.library

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tachiyomi.domain.library.model.Library
import tachiyomi.domain.library.model.LibraryCategory
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.R
import tachiyomi.ui.glide.GlideRequests
import tachiyomi.ui.util.inflate
import tachiyomi.ui.widget.ViewPagerAdapter

class LibraryAdapter(
  private val listener: Listener,
  private val glideRequests: GlideRequests
) : ViewPagerAdapter() {

  private var items = emptyList<LibraryCategory>()
  private var selectedManga = emptySet<Long>()

  private val pool = RecyclerView.RecycledViewPool()

  private var boundViews = mutableListOf<LibraryCategoryView>()

  override fun createView(container: ViewGroup, position: Int): View {
    val view = container.inflate(R.layout.library_category_view) as LibraryCategoryView
    view.create(pool, glideRequests, listener)
    view.bind(items[position], selectedManga)
    boundViews.add(view)
    return view
  }

  override fun destroyView(container: ViewGroup, position: Int, view: View) {
    boundViews.remove(view as LibraryCategoryView)
    super.destroyView(container, position, view)
  }

  override fun getCount(): Int {
    return items.size
  }

  fun setItems(items: Library, selectedManga: Set<Long>) {
    val categoriesChanged = categoriesChanged(this.items, items)
    this.items = items
    this.selectedManga = selectedManga

    if (categoriesChanged) {
      notifyDataSetChanged()
    }

    for (view in boundViews) {
      val libCategory = items.find { it.category.id == view.libCategory.category.id }
      if (libCategory != null) {
        view.bind(libCategory, selectedManga)
      }
    }
  }

  override fun getItemPosition(obj: Any): Int {
    val view = obj as? LibraryCategoryView ?: return POSITION_NONE
    val index = items.indexOfFirst { it.category.id == view.libCategory.category.id }
    return if (index == -1) POSITION_NONE else index
  }

  private fun categoriesChanged(oldLibrary: Library, newLibrary: Library): Boolean {
    if (oldLibrary === newLibrary) return false
    if (oldLibrary.size != newLibrary.size) return true

    oldLibrary.forEachIndexed { i, oldCategory ->
      val newCategory = newLibrary[i]
      if (oldCategory.category.id != newCategory.category.id) return true
    }

    return false
  }

  interface Listener {
    fun onMangaClick(manga: LibraryManga)
    fun onMangaLongClick(manga: LibraryManga)
  }

}
