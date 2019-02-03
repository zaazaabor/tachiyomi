/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalogdetail

import tachiyomi.core.di.bindInstance
import toothpick.config.Module

class CatalogDetailsModule(controller: CatalogDetailsController) : Module() {

  init {
    val params = CatalogDetailsParams(controller.getCatalogPkgName())
    bindInstance(params)
  }

}
