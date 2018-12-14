/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.glide

import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import tachiyomi.domain.catalog.model.Catalog

@Suppress("unused")
internal fun ModelLoader<out Catalog, *>.getCatalogKey(model: Catalog): ObjectKey {
  return ObjectKey(when (model) {
    is Catalog.Internal -> "catalog://${model.source.id}"
    is Catalog.Installed -> "catalog://${model.pkgName}/${model.versionCode}"
    is Catalog.Available -> model.iconUrl
  })
}
