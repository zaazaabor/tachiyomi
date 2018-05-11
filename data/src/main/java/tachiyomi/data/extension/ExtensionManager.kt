package tachiyomi.data.extension

import android.app.Application
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.experimental.async
import tachiyomi.core.http.Http
import tachiyomi.core.util.launchNow
import tachiyomi.data.extension.api.ExtensionGithubApi
import tachiyomi.data.extension.model.Extension
import tachiyomi.data.extension.model.InstallStep
import tachiyomi.data.extension.model.LoadResult
import tachiyomi.data.extension.prefs.ExtensionPreferences
import tachiyomi.data.extension.util.ExtensionInstallReceiver
import tachiyomi.data.extension.util.ExtensionInstaller
import tachiyomi.data.extension.util.ExtensionLoader
import tachiyomi.domain.source.SourceManager
import javax.inject.Inject

/**
 * The manager of extensions installed as another apk which extend the available sources. It handles
 * the retrieval of remotely available extensions as well as installing, updating and removing them.
 * To avoid malicious distribution, every extension must be signed and it will only be loaded if its
 * signature is trusted, otherwise the user will be prompted with a warning to trust it before being
 * loaded.
 *
 * @param context The application context.
 * @param preferences The application preferences.
 */
class ExtensionManager @Inject internal constructor(
  private val context: Application,
  private val http: Http,
  private val preferences: ExtensionPreferences
) {

  /**
   * API where all the available extensions can be found.
   */
  private val api = ExtensionGithubApi(http)

  /**
   * The installer which installs, updates and uninstalls the extensions.
   */
  private val installer by lazy { ExtensionInstaller(context) }

  /**
   * Relay used to notify the installed extensions.
   */
  private val installedExtensionsRelay = BehaviorSubject.create<List<Extension.Installed>>()

  /**
   * List of the currently installed extensions.
   */
  var installedExtensions = emptyList<Extension.Installed>()
    private set(value) {
      field = value
      installedExtensionsRelay.onNext(value)
    }

  /**
   * Relay used to notify the available extensions.
   */
  private val availableExtensionsRelay = BehaviorSubject.create<List<Extension.Available>>()

  /**
   * List of the currently available extensions.
   */
  var availableExtensions = emptyList<Extension.Available>()
    private set(value) {
      field = value
      availableExtensionsRelay.onNext(value)
      setUpdateFieldOfInstalledExtensions(value)
    }

  /**
   * Relay used to notify the untrusted extensions.
   */
  private val untrustedExtensionsRelay = BehaviorSubject.create<List<Extension.Untrusted>>()

  /**
   * List of the currently untrusted extensions.
   */
  var untrustedExtensions = emptyList<Extension.Untrusted>()
    private set(value) {
      field = value
      untrustedExtensionsRelay.onNext(value)
    }

  /**
   * The source manager where the sources of the extensions are added.
   */
  private lateinit var sourceManager: SourceManager

  /**
   * Initializes this manager with the given source manager.
   */
  fun init(sourceManager: SourceManager) {
    this.sourceManager = sourceManager
    initExtensions()
    ExtensionInstallReceiver(InstallationListener()).register(context)
  }

  /**
   * Loads and registers the installed extensions.
   */
  private fun initExtensions() {
    val extensions = ExtensionLoader.loadExtensions(context)

    installedExtensions = extensions
      .filterIsInstance<LoadResult.Success>()
      .map { it.extension }
    installedExtensions
      .flatMap { it.sources }
      // overwrite is needed until the bundled sources are removed
      .forEach { sourceManager.registerSource(it, true) }

    untrustedExtensions = extensions
      .filterIsInstance<LoadResult.Untrusted>()
      .map { it.extension }
  }

  /**
   * Returns the relay of the installed extensions as an observable.
   */
  fun getInstalledExtensionsObservable(): Observable<List<Extension.Installed>> {
    return installedExtensionsRelay
  }

  /**
   * Returns the relay of the available extensions as an observable.
   */
  fun getAvailableExtensionsObservable(): Observable<List<Extension.Available>> {
    return availableExtensionsRelay
  }

  /**
   * Returns the relay of the untrusted extensions as an observable.
   */
  fun getUntrustedExtensionsObservable(): Observable<List<Extension.Untrusted>> {
    return untrustedExtensionsRelay
  }

  /**
   * Finds the available extensions in the [api] and updates [availableExtensions].
   */
  fun findAvailableExtensions() {
    api.findExtensions()
      .onErrorReturn { emptyList() }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { availableExtensions = it }
  }

  /**
   * Sets the update field of the installed extensions with the given [availableExtensions].
   *
   * @param availableExtensions The list of extensions given by the [api].
   */
  private fun setUpdateFieldOfInstalledExtensions(availableExtensions: List<Extension.Available>) {
    val mutInstalledExtensions = installedExtensions.toMutableList()
    var changed = false

    for ((index, installedExt) in mutInstalledExtensions.withIndex()) {
      val pkgName = installedExt.pkgName
      val availableExt = availableExtensions.find { it.pkgName == pkgName } ?: continue

      val hasUpdate = availableExt.versionCode > installedExt.versionCode
      if (installedExt.hasUpdate != hasUpdate) {
        mutInstalledExtensions[index] = installedExt.copy(hasUpdate = hasUpdate)
        changed = true
      }
    }
    if (changed) {
      installedExtensions = mutInstalledExtensions
    }
  }

  /**
   * Returns an observable of the installation process for the given extension. It will complete
   * once the extension is installed or throws an error. The process will be canceled if
   * unsubscribed before its completion.
   *
   * @param extension The extension to be installed.
   */
  fun installExtension(extension: Extension.Available): Observable<InstallStep> {
    return installer.downloadAndInstall(api.getApkUrl(extension), extension)
  }

  /**
   * Returns an observable of the installation process for the given extension. It will complete
   * once the extension is updated or throws an error. The process will be canceled if
   * unsubscribed before its completion.
   *
   * @param extension The extension to be updated.
   */
  fun updateExtension(extension: Extension.Installed): Observable<InstallStep> {
    val availableExt = availableExtensions.find { it.pkgName == extension.pkgName }
                       ?: return Observable.empty()
    return installExtension(availableExt)
  }

  /**
   * Sets the result of the installation of an extension.
   *
   * @param downloadId The id of the download.
   * @param result Whether the extension was installed or not.
   */
  fun setInstallationResult(downloadId: Long, result: Boolean) {
    installer.setInstallationResult(downloadId, result)
  }

  /**
   * Uninstalls the extension that matches the given package name.
   *
   * @param pkgName The package name of the application to uninstall.
   */
  fun uninstallExtension(pkgName: String) {
    installer.uninstallApk(pkgName)
  }

  /**
   * Adds the given signature to the list of trusted signatures. It also loads in background the
   * extensions that match this signature.
   *
   * @param signature The signature to whitelist.
   */
  fun trustSignature(signature: String) {
    val untrustedSignatures = untrustedExtensions.map { it.signatureHash }.toSet()
    if (signature !in untrustedSignatures) return

    ExtensionLoader.trustedSignatures += signature
    val preference = preferences.trustedSignatures()
    preference.set(preference.get() + signature)

    val nowTrustedExtensions = untrustedExtensions.filter { it.signatureHash == signature }
    untrustedExtensions -= nowTrustedExtensions

    val ctx = context
    launchNow {
      nowTrustedExtensions
        .map { extension ->
          async { ExtensionLoader.loadExtensionFromPkgName(ctx, extension.pkgName) }
        }
        .map { it.await() }
        .forEach { result ->
          if (result is LoadResult.Success) {
            registerNewExtension(result.extension)
          }
        }
    }
  }

  /**
   * Registers the given extension in this and the source managers.
   *
   * @param extension The extension to be registered.
   */
  private fun registerNewExtension(extension: Extension.Installed) {
    installedExtensions += extension
    extension.sources.forEach { sourceManager.registerSource(it) }
  }

  /**
   * Registers the given updated extension in this and the source managers previously removing
   * the outdated ones.
   *
   * @param extension The extension to be registered.
   */
  private fun registerUpdatedExtension(extension: Extension.Installed) {
    val mutInstalledExtensions = installedExtensions.toMutableList()
    val oldExtension = mutInstalledExtensions.find { it.pkgName == extension.pkgName }
    if (oldExtension != null) {
      mutInstalledExtensions -= oldExtension
      extension.sources.forEach { sourceManager.unregisterSource(it) }
    }
    mutInstalledExtensions += extension
    installedExtensions = mutInstalledExtensions
    extension.sources.forEach { sourceManager.registerSource(it) }
  }

  /**
   * Unregisters the extension in this and the source managers given its package name. Note this
   * method is called for every uninstalled application in the system.
   *
   * @param pkgName The package name of the uninstalled application.
   */
  private fun unregisterExtension(pkgName: String) {
    val installedExtension = installedExtensions.find { it.pkgName == pkgName }
    if (installedExtension != null) {
      installedExtensions -= installedExtension
      installedExtension.sources.forEach { sourceManager.unregisterSource(it) }
    }
    val untrustedExtension = untrustedExtensions.find { it.pkgName == pkgName }
    if (untrustedExtension != null) {
      untrustedExtensions -= untrustedExtension
    }
  }

  /**
   * Listener which receives events of the extensions being installed, updated or removed.
   */
  private inner class InstallationListener : ExtensionInstallReceiver.Listener {

    override fun onExtensionInstalled(extension: Extension.Installed) {
      registerNewExtension(extension.withUpdateCheck())
    }

    override fun onExtensionUpdated(extension: Extension.Installed) {
      registerUpdatedExtension(extension.withUpdateCheck())
    }

    override fun onExtensionUntrusted(extension: Extension.Untrusted) {
      untrustedExtensions += extension
    }

    override fun onPackageUninstalled(pkgName: String) {
      unregisterExtension(pkgName)
    }
  }

  /**
   * Extension method to set the update field of an installed extension.
   */
  private fun Extension.Installed.withUpdateCheck(): Extension.Installed {
    val availableExt = availableExtensions.find { it.pkgName == pkgName }
    if (availableExt != null && availableExt.versionCode > versionCode) {
      return copy(hasUpdate = true)
    }
    return this
  }

}
