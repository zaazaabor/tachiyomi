/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.settings

import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import com.bluelinelabs.conductor.Conductor
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import tachiyomi.core.ui.R
import tachiyomi.ui.controller.withoutTransition
import tachiyomi.ui.cyanea.CyaneaThemePickerController

open class ThemePickerActivity : CyaneaAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val container = findViewById<ViewGroup>(android.R.id.content)
    val router = Conductor.attachRouter(this, container, savedInstanceState)

    if (!router.hasRootController()) {
      router.setRoot(CyaneaThemePickerController().withoutTransition())
    }
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    return when (item?.itemId) {
      android.R.id.home -> {
        onBackPressed()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    overridePendingTransition(R.anim.enter_left, R.anim.exit_right)
  }

}
