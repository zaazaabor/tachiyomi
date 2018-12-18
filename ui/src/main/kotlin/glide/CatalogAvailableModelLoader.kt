/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.glide

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader
import tachiyomi.domain.catalog.model.Catalog
import java.io.InputStream

internal class CatalogAvailableModelLoader(
  urlLoader: ModelLoader<GlideUrl, InputStream>
) : BaseGlideUrlLoader<Catalog.Available>(urlLoader) {

  override fun getUrl(
    model: Catalog.Available,
    width: Int,
    height: Int,
    options: Options?
  ): String {
    return model.iconUrl
  }

  override fun handles(model: Catalog.Available): Boolean {
    return true
  }

  class Factory : ModelLoaderFactory<Catalog.Available, InputStream> {

    override fun build(factory: MultiModelLoaderFactory): ModelLoader<Catalog.Available, InputStream> {
      val loader = factory.build(GlideUrl::class.java, InputStream::class.java)
      return CatalogAvailableModelLoader(loader)
    }

    override fun teardown() {
    }

  }

}
