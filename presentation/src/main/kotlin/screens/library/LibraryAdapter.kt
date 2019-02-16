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

  private val pool = RecyclerView.RecycledViewPool()

  override fun createView(container: ViewGroup, position: Int): View {
    val view = container.inflate(R.layout.library_category_view) as LibraryCategoryView
    view.bind(items[position], pool, glideRequests, listener)
    return view
  }

  override fun getCount(): Int {
    return items.size
  }

  fun setItems(items: List<LibraryCategory>) {
    this.items = items
    notifyDataSetChanged()
  }

  override fun getPageTitle(position: Int): CharSequence? {
    return items[position].category.name
  }

  override fun getItemPosition(obj: Any): Int {
    val view = obj as? LibraryCategoryView ?: return POSITION_NONE
    val index = items.indexOfFirst { it.category.id == view.category?.category?.id }
    return if (index == -1) POSITION_NONE else index
  }

  interface Listener {
    fun onMangaClick(manga: LibraryManga)
  }

}
