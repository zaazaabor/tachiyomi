/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.di

import toothpick.config.Binding
import toothpick.config.Module
import javax.inject.Provider

/**
 * Binds the given [instance] to its class.
 */
inline fun <reified B> Module.bindInstance(instance: B) {
  bind(B::class.java).toInstance(instance)
}

inline fun <reified B, reified D : B> Module.bindTo(): Binding<B>.BoundStateForClassBinding {
  return bind(B::class.java).to(D::class.java)
}

inline fun <reified B, reified P : Provider<B>> Module.bindProvider(): Binding<B>
.BoundStateForProviderClassBinding {
  return bind(B::class.java).toProvider(P::class.java)
}

inline fun <reified B> Module.bindProviderInstance(
  noinline provider: () -> B
): Binding<B>.BoundStateForProviderInstanceBinding? {
  return bind(B::class.java).toProviderInstance(provider)
}
