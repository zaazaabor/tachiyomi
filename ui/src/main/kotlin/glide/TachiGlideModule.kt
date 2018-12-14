/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.glide

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import tachiyomi.core.di.AppScope
import tachiyomi.core.http.Http
import tachiyomi.domain.catalog.model.Catalog
import java.io.InputStream
import javax.inject.Inject

/**
 * Class used to update Glide module settings
 */
@GlideModule
class TachiGlideModule : AppGlideModule() {

  @Inject
  lateinit var http: Http

  init {
    AppScope.inject(this)
  }

  override fun applyOptions(context: Context, builder: GlideBuilder) {
    builder.setDiskCache(InternalCacheDiskCacheFactory(context, 50 * 1024 * 1024))
    builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
    builder.setDefaultTransitionOptions(Drawable::class.java,
      DrawableTransitionOptions.withCrossFade())
  }

  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    val networkFactory = OkHttpUrlLoader.Factory(http.defaultClient)
    val internalCatalogFactory = CatalogInternalModelLoader.Factory()
    val installedCatalogFactory = CatalogInstalledModelLoader.Factory(context)

    registry.replace(GlideUrl::class.java, InputStream::class.java, networkFactory)
    registry.append(Catalog.Internal::class.java, Drawable::class.java, internalCatalogFactory)
    registry.append(Catalog.Installed::class.java, Drawable::class.java, installedCatalogFactory)
//    registry.append(Manga::class.java, InputStream::class.java, MangaModelLoader.Factory())
  }
}
