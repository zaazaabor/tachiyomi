/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.controller

import android.transition.Fade
import android.transition.Transition
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler

fun Controller.withHorizontalTransition(): RouterTransaction {
  return RouterTransaction.with(this)
    .pushChangeHandler(HorizontalChangeHandler())
    .popChangeHandler(HorizontalChangeHandler())
}

fun Controller.withFadeTransition(): RouterTransaction {
  return RouterTransaction.with(this)
    .pushChangeHandler(FadeTransition())
    .popChangeHandler(FadeTransition())
}

fun Controller.withoutTransition(): RouterTransaction {
  return RouterTransaction.with(this)
}

class FadeTransition : SimpleTransitionChangeHandler() {

  override fun getExitTransition(
    container: ViewGroup,
    from: View?,
    to: View?,
    isPush: Boolean
  ): Transition? {
    return null
  }

  override fun getEnterTransition(
    container: ViewGroup,
    from: View?,
    to: View?,
    isPush: Boolean
  ): Transition? {
    return Fade(Fade.MODE_IN).excludeTarget(Toolbar::class.java, true)
  }

}
