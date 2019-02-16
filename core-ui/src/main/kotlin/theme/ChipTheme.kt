/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.theme

import android.content.Context
import android.content.res.ColorStateList
import com.jaredrummler.cyanea.Cyanea
import tachiyomi.ui.cyanea.backgroundColorAlt
import tachiyomi.ui.cyanea.textColorForAccent
import tachiyomi.ui.util.getColorFromAttr

class ChipTheme : Theme {

  class SelectedAccent(context: Context) {

    private val cyanea get() = Cyanea.instance

    private val states = arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf())

    val backgroundColor = ColorStateList(
      states,
      intArrayOf(cyanea.accent, cyanea.backgroundColorAlt)
    )

    val textColor = ColorStateList(
      states,
      intArrayOf(
        cyanea.textColorForAccent,
        context.getColorFromAttr(android.R.attr.textColorPrimary)
      )
    )
  }

}
