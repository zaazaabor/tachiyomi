/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.settings

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import com.bluelinelabs.conductor.Conductor
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import com.jaredrummler.cyanea.prefs.CyaneaThemePickerLauncher
import tachiyomi.core.ui.R
import tachiyomi.ui.controller.withoutTransition
import tachiyomi.ui.theme.CyaneaSettingsController

open class AppearanceActivity : CyaneaAppCompatActivity(), CyaneaThemePickerLauncher {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val container = findViewById<ViewGroup>(android.R.id.content)
    val router = Conductor.attachRouter(this, container, savedInstanceState)

    if (!router.hasRootController()) {
      router.setRoot(CyaneaSettingsController().withoutTransition())
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

  override fun launchThemePicker() {
    startActivity(Intent(this, ThemePickerActivity::class.java))
    overridePendingTransition(R.anim.enter_right, R.anim.exit_left)
  }

}
