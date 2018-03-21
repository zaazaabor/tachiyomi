package tachiyomi

import android.app.Application
import tachiyomi.app.BuildConfig
import tachiyomi.app.FactoryRegistry
import tachiyomi.app.MemberInjectorRegistry
import tachiyomi.core.http.HttpModule
import tachiyomi.core.js.JSModule
import tachiyomi.data.di.DataModule
import timber.log.Timber
import toothpick.Scope
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.registries.FactoryRegistryLocator
import toothpick.registries.MemberInjectorRegistryLocator
import toothpick.smoothie.module.SmoothieApplicationModule

class App : Application() {

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    Toothpick.setConfiguration(Configuration.forDevelopment().disableReflection())
    FactoryRegistryLocator.setRootRegistry(FactoryRegistry())
    MemberInjectorRegistryLocator.setRootRegistry(MemberInjectorRegistry())

    val scope = Toothpick.openScope(AppScope)
    scope.installModules(SmoothieApplicationModule(this), HttpModule, JSModule, DataModule)
  }

}

object AppScope

fun applicationScope(vararg any: Any): Scope {
  return Toothpick.openScopes(AppScope, any)
}
