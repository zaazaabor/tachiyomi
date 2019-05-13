/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.controller

import android.os.Bundle
import tachiyomi.core.di.AppScope
import tachiyomi.ui.presenter.BasePresenter
import toothpick.Toothpick
import toothpick.config.Module

abstract class MvpController<P : BasePresenter>(
  bundle: Bundle? = null
) : BaseController(bundle) {

  @Suppress("LeakingThis")
  protected val scope = AppScope.subscope(this).also { scope ->
    getModule()?.let { scope.installModules(it) }
  }

  val presenter: P by lazy { scope.getInstance(getPresenterClass()) }

  abstract fun getPresenterClass(): Class<P>

  open fun getModule(): Module? {
    return null
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.destroy()
    // TODO we might have to join before closing this scope to allow current jobs to finish
    // (they might need to inject a dependency if using a Provider)
    Toothpick.closeScope(this)
  }

}
