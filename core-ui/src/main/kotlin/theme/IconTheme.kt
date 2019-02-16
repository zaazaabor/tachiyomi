/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.theme

import android.content.Context
import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.core.content.ContextCompat
import tachiyomi.core.ui.R

class IconTheme(context: Context) : Theme {

  val tintColor = ContextCompat.getColor(context, if (cyanea.isDark) {
    R.color.textColorIconInverse
  } else {
    R.color.textColorIcon
  })

  fun apply(imageView: ImageView) {
    imageView.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
  }

}
