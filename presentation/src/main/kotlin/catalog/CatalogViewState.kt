/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalog

import tachiyomi.domain.catalog.model.InstallStep

data class ViewState(
  val items: List<Any> = emptyList(),
  val languageChoice: LanguageChoice = LanguageChoice.All,
  val installingCatalogs: Map<String, InstallStep> = emptyMap(),
  val isRefreshing: Boolean = false
)
