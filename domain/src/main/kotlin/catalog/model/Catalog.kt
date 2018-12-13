/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.catalog.model

import tachiyomi.source.Source

sealed class Catalog {

  abstract val name: String
  abstract val pkgName: String
  abstract val versionName: String
  abstract val versionCode: Int

  data class BuiltIn(
    override val name: String,
    override val pkgName: String,
    override val versionName: String,
    override val versionCode: Int,
    val source: Source
  ) : Catalog()

  data class Installed(
    override val name: String,
    override val pkgName: String,
    override val versionName: String,
    override val versionCode: Int,
    val source: Source,
    val hasUpdate: Boolean = false
  ) : Catalog()

  data class Available(
    override val name: String,
    override val pkgName: String,
    override val versionName: String,
    override val versionCode: Int,
    val apkName: String,
    val iconUrl: String
  ) : Catalog()

}
