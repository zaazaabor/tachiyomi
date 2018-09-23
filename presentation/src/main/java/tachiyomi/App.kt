package tachiyomi

import android.app.Application
import android.os.Looper
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import tachiyomi.app.BuildConfig
import tachiyomi.app.FactoryRegistry
import tachiyomi.app.MemberInjectorRegistry
import tachiyomi.core.di.AppScope
import tachiyomi.core.http.HttpModule
import tachiyomi.data.di.DataModule
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.registries.FactoryRegistryLocator
import toothpick.registries.MemberInjectorRegistryLocator
import toothpick.smoothie.module.SmoothieApplicationModule
import java.io.IOException
import java.net.SocketException

@Suppress("unused")
class App : Application() {

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    initRxJava()

    Toothpick.setConfiguration(Configuration.forDevelopment().disableReflection())
    FactoryRegistryLocator.setRootRegistry(FactoryRegistry())
    MemberInjectorRegistryLocator.setRootRegistry(MemberInjectorRegistry())

    val scope = Toothpick.openScope(AppScope)
    scope.installModules(SmoothieApplicationModule(this), HttpModule, DataModule)
  }

  private fun initRxJava() {
    // Init async scheduler
    val asyncMainThreadScheduler = AndroidSchedulers.from(Looper.getMainLooper(), true)
    RxAndroidPlugins.setInitMainThreadSchedulerHandler { asyncMainThreadScheduler }

    // Install default error handler
    RxJavaPlugins.setErrorHandler { e ->
      var error = e
      if (error is UndeliverableException) {
        error = error.cause
      }
      if (error is InterruptedException || error is IOException || error is SocketException) {
        // fine, irrelevant network problem or API that throws on cancellation or
        // some blocking code was interrupted by a dispose call
      }
      if ((error is NullPointerException) || (error is IllegalArgumentException)) {
        // that's likely a bug in the application
        Thread.currentThread().uncaughtExceptionHandler
          .uncaughtException(Thread.currentThread(), error)
      }
      if (error is IllegalStateException) {
        // that's a bug in RxJava or in a custom operator
        Thread.currentThread().uncaughtExceptionHandler
          .uncaughtException(Thread.currentThread(), error)
      }
      Timber.w(error, "Undeliverable exception received, not sure what to do")
    }
  }

}
