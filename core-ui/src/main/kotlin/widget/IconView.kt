/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.ColorUtils
import androidx.core.widget.ImageViewCompat
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.app.BaseCyaneaActivity

class IconView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

  init {
    applyTint()
  }

  fun applyTint() {
    val cyanea = (context as? BaseCyaneaActivity)?.cyanea ?: Cyanea.instance

    val states = arrayOf(
      intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused),
      intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_focused),
      intArrayOf()
    )

    // TODO tint should be applied to the current background of this icon
    // Refer to: https://material.io/design/iconography/system-icons.html#color
    val colors = if (!cyanea.isDark) {
      intArrayOf(
        // 87% black
        Color.BLACK.let { ColorUtils.setAlphaComponent(it, 222) },
        // 54% black
        Color.BLACK.let { ColorUtils.setAlphaComponent(it, 138) },
        // 38% black
        Color.BLACK.let { ColorUtils.setAlphaComponent(it, 97) }
      )
    } else {
      intArrayOf(
        // 100% white
        Color.WHITE,
        // 70% white
        Color.WHITE.let { ColorUtils.setAlphaComponent(it, 179) },
        // 50% white
        Color.WHITE.let { ColorUtils.setAlphaComponent(it, 128) }
      )
    }

    val colorStateList = ColorStateList(states, colors)

    ImageViewCompat.setImageTintList(this, colorStateList)
  }

}
