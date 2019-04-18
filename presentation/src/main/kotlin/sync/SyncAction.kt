/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.sync

sealed class Action {

  data class Login(val address: String, val username: String, val password: String) : Action() {
    override fun toString(): String {
      return "Login(address='$address', username='$username', password='secret')"
    }
  }

  data class Loading(val isLoading: Boolean) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(isLoading = isLoading)
  }

  data class Logged(val isLogged: Boolean) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(isLogged = isLogged)
  }

  open fun reduce(state: ViewState) = state
}
