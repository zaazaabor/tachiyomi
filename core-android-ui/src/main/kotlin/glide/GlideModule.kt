/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import tachiyomi.core.di.AppScope
import javax.inject.Inject

/**
 * Class used to update Glide module settings
 */
@GlideModule
internal class GlideModule : AppGlideModule() {

  @Inject
  lateinit var callback: GlideInitCallback

  init {
    AppScope.inject(this)
  }

  override fun applyOptions(context: Context, builder: GlideBuilder) {
    callback.onApplyOptions(context, builder)

  }

  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    callback.onRegisterComponents(context, glide, registry)
  }
}
