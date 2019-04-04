/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceController
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.settings_controller.*
import tachiyomi.ui.R
import tachiyomi.ui.controller.BaseController
import tachiyomi.ui.controller.withHorizontalTransition
import tachiyomi.ui.controller.withoutTransition
import tachiyomi.ui.home.HomeChildController
import tachiyomi.ui.sync.SyncController

class SettingsController : BaseController(), HomeChildController {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.settings_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    val childRouter = getChildRouter(settings_container)
    if (!childRouter.hasRootController()) {
      childRouter.pushController(ContentController().withoutTransition())
    }
  }

  class ContentController : PreferenceController(), HomeChildController {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      addPreferencesFromResource(R.xml.settings_general)

      val appearance = findPreference("prefmain_appearance")
      appearance.icon.setTint(Cyanea.instance.accent)
      appearance.setOnPreferenceClickListener {
        activity?.run {
          startActivity(Intent(this, AppearanceActivity::class.java))
          overridePendingTransition(R.anim.enter_right, R.anim.exit_left)
        }
        true
      }

      val sync = findPreference("prefmain_sync")
      sync.icon.setTint(Cyanea.instance.accent)
      sync.setOnPreferenceClickListener {
        parentController?.router?.pushController(SyncController().withHorizontalTransition())
        true
      }
    }

  }

}
