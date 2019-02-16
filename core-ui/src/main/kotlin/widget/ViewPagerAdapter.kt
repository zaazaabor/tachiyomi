/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.widget

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

abstract class ViewPagerAdapter : PagerAdapter() {

  protected abstract fun createView(container: ViewGroup, position: Int): View

  protected open fun destroyView(container: ViewGroup, position: Int, view: View) {
  }

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val view = createView(container, position)
    container.addView(view)
    return view
  }

  override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
    val view = obj as View
    destroyView(container, position, view)
    container.removeView(view)
  }

  override fun isViewFromObject(view: View, obj: Any): Boolean {
    return view === obj
  }

  interface PositionableView {
    val item: Any
  }

}
