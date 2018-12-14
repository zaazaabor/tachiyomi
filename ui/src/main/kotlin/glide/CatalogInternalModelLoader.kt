/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.glide

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.widget.TextOvalDrawable

internal class CatalogInternalModelLoader : ModelLoader<Catalog.Internal, Drawable> {

  override fun buildLoadData(
    model: Catalog.Internal,
    width: Int,
    height: Int,
    options: Options
  ): ModelLoader.LoadData<Drawable>? {
    val key = getCatalogKey(model)
    val fetcher = Fetcher(model)
    return ModelLoader.LoadData(key, fetcher)
  }

  override fun handles(model: Catalog.Internal): Boolean {
    return true
  }

  private class Fetcher(
    private val model: Catalog.Internal
  ) : DataFetcher<Drawable> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Drawable>) {
      val sourceName = model.source.name
      val text = if (sourceName.isNotEmpty()) {
        sourceName.take(1)
      } else {
        ""
      }

      val drawable = TextOvalDrawable(
        text = text,
        backgroundColor = TextOvalDrawable.Colors.getColor(sourceName),
        textColor = Color.WHITE
      )

      callback.onDataReady(drawable)
    }

    override fun getDataClass(): Class<Drawable> {
      return Drawable::class.java
    }

    override fun getDataSource(): DataSource {
      return DataSource.DATA_DISK_CACHE
    }

    override fun cleanup() {
    }

    override fun cancel() {
    }
  }

  class Factory : ModelLoaderFactory<Catalog.Internal, Drawable> {

    override fun build(factory: MultiModelLoaderFactory): ModelLoader<Catalog.Internal, Drawable> {
      return CatalogInternalModelLoader()
    }

    override fun teardown() {

    }

  }

}
