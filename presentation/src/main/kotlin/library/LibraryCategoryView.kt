/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tachiyomi.domain.library.model.LibraryCategory
import tachiyomi.ui.glide.GlideRequests

class LibraryCategoryView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

  private lateinit var adapter: LibraryCategoryAdapter

  lateinit var libCategory: LibraryCategory
    private set

  private val recycler by lazy(LazyThreadSafetyMode.NONE) {
    (getChildAt(0) as RecyclerView).also {
      it.layoutManager = GridLayoutManager(context, 2)
    }
  }

  fun create(
    pool: RecyclerView.RecycledViewPool,
    glideRequests: GlideRequests,
    listener: LibraryAdapter.Listener
  ) {
    recycler.setRecycledViewPool(pool)
    adapter = LibraryCategoryAdapter(glideRequests, listener)
    recycler.adapter = adapter
  }

  fun bind(category: LibraryCategory, selectedManga: Set<Long>) {
    this.libCategory = category
    adapter.submitManga(category.mangas, selectedManga)
  }

}
