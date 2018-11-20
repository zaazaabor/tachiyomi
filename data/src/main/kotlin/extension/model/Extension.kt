/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.extension.model

import tachiyomi.source.Source

sealed class Extension {

  abstract val name: String
  abstract val pkgName: String
  abstract val versionName: String
  abstract val versionCode: Int
  abstract val lang: String?

  data class Installed(
    override val name: String,
    override val pkgName: String,
    override val versionName: String,
    override val versionCode: Int,
    val sources: List<Source>,
    override val lang: String,
    val hasUpdate: Boolean = false
  ) : Extension()

  data class Available(
    override val name: String,
    override val pkgName: String,
    override val versionName: String,
    override val versionCode: Int,
    override val lang: String,
    val apkName: String,
    val iconUrl: String
  ) : Extension()

  data class Untrusted(
    override val name: String,
    override val pkgName: String,
    override val versionName: String,
    override val versionCode: Int,
    val signatureHash: String,
    override val lang: String? = null
  ) : Extension()

}
