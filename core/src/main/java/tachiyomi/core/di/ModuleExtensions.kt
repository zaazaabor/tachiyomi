package tachiyomi.core.di

import toothpick.config.Module

/**
 * Binds the given [instance] to its class.
 */
inline fun <reified T> Module.bindInstance(instance: T) {
  bind(T::class.java).toInstance(instance)
}
