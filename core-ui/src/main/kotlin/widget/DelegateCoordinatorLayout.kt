/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import tachiyomi.core.ui.R

class DelegateCoordinatorLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  @AttrRes
  @SuppressLint("PrivateResource")
  defStyleAttr: Int = R.attr.coordinatorLayoutStyle
) : CoordinatorLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

  init {
    doOnPreDraw {
      collectDelegates()
    }
  }

  override fun onViewAdded(child: View?) {
    super.onViewAdded(child)
    collectDelegates()
  }

  override fun onViewRemoved(child: View?) {
    super.onViewRemoved(child)
    collectDelegates()
  }

  override fun getBehavior(): Behavior<*> {
    return DelegateBehavior()
  }

  private fun getDelegateBehavior(): DelegateBehavior? {
    val behavior = (layoutParams as? LayoutParams)?.behavior
    return behavior as? DelegateBehavior
  }

  private fun collectDelegates() {
    getDelegateBehavior()?.collectDelegates(this)
  }

  class DelegateBehavior : CoordinatorLayout.Behavior<CoordinatorLayout>() {

    private val delegates = mutableListOf<Pair<View, Behavior<View>>>()

    fun collectDelegates(coordinatorLayout: CoordinatorLayout) {
      coordinatorLayout.children.mapNotNullTo(delegates) {
        val behavior = (it.layoutParams as? LayoutParams)?.behavior
        if (behavior != null) it to behavior else null
      }
    }

    override fun onStartNestedScroll(
      coordinatorLayout: CoordinatorLayout,
      child: CoordinatorLayout,
      directTargetChild: View,
      target: View,
      axes: Int,
      type: Int
    ): Boolean {
      return delegates.any { (view, behavior) ->
        behavior.onStartNestedScroll(coordinatorLayout, view, directTargetChild, target, axes,
          type)
      }
    }

    override fun onNestedScroll(
      coordinatorLayout: CoordinatorLayout,
      child: CoordinatorLayout,
      target: View,
      dxConsumed: Int,
      dyConsumed: Int,
      dxUnconsumed: Int,
      dyUnconsumed: Int,
      type: Int
    ) {
      delegates.forEach { (view, behavior) ->
        behavior.onNestedScroll(coordinatorLayout, view, target, dxConsumed, dyConsumed,
          dxUnconsumed, dyUnconsumed, type)
      }
    }

  }

}
