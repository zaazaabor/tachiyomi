/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.home

import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding2.support.v7.widget.navigationClicks
import tachiyomi.ui.controller.BaseController
import tachiyomi.ui.util.getDrawableAttr

interface HomeChildController {

  interface FAB : HomeChildController {
    fun createFAB(container: ViewGroup): FloatingActionButton
  }

  fun BaseController.setupToolbarNavWithHomeController(toolbar: Toolbar) {
    val homeCtrl = parentController as? HomeController ?: return
    val homeRouter = homeCtrl.childRouters.firstOrNull() ?: return

    if (homeRouter.backstackSize > 1) {
      toolbar.navigationIcon = toolbar.context.getDrawableAttr(android.R.attr.homeAsUpIndicator)
      toolbar.navigationClicks().subscribeWithView { router.handleBack() }
    }
  }

}
