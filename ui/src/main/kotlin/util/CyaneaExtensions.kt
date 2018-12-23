/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.util

import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.utils.ColorUtils
import tachiyomi.ui.R

@Suppress("DEPRECATION")
val Cyanea.textColorForAccent: Int
  get() = Cyanea.res.getColor(if (ColorUtils.isDarkColor(accent)) {
    R.color.textColorPrimaryInverse
  } else {
    R.color.textColorPrimary
  })

val Cyanea.backgroundColorAlt: Int
  get() = if (isDark) backgroundColorLight else backgroundColorDark
