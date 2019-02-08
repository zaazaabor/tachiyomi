/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.installer

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.CompletableSubject
import tachiyomi.core.util.getUriCompat
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import timber.log.Timber
import timber.log.error
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */
internal class CatalogInstaller @Inject constructor(private val context: Application) {

  /**
   * The system's download manager.
   */
  private val downloadManager =
    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

  /**
   * The broadcast receiver which listens to download completion events.
   */
  private val downloadReceiver = DownloadCompletionReceiver()

  /**
   * The currently requested downloads, with the package name (unique id) as key, and the id
   * returned by the download manager.
   */
  private val activeDownloads = hashMapOf<String, Long>()

  /**
   * Relay used to notify the installation step of every download.
   */
  private val downloadsRelay = PublishRelay.create<Pair<Long, InstallStep>>()

  private val uninstallIds = AtomicLong()
  private val activeUninstalls = hashMapOf<Long, CompletableSubject>()

  /**
   * Adds the given extension to the downloads queue and returns an observable containing its
   * step in the installation process.
   *
   * @param catalog The catalog to install.
   */
  fun downloadAndInstall(catalog: CatalogRemote) = Observable.defer {
    val pkgName = catalog.pkgName

    val oldDownload = activeDownloads[pkgName]
    if (oldDownload != null) {
      deleteDownload(pkgName)
    }

    // Register the receiver after removing (and unregistering) the previous download
    downloadReceiver.register()

    val request = DownloadManager.Request(Uri.parse(catalog.apkUrl))
      .setTitle(catalog.name)
      .setMimeType(APK_MIME)
      .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

    val id = downloadManager.enqueue(request)
    activeDownloads[pkgName] = id

    downloadsRelay.filter { it.first == id }
      .map { it.second }
      // Poll download status
      .mergeWith(pollStatus(id))
      // Force an error if the download takes more than 3 minutes
      .mergeWith(Observable.timer(3, TimeUnit.MINUTES).map { InstallStep.Error })
      // Stop when the application is installed or fails
      .takeUntil { it.isCompleted() }
      // Always notify on main thread
      .observeOn(AndroidSchedulers.mainThread())
      // Always remove the download when unsubscribed
      .doFinally { deleteDownload(pkgName) }
  }

  /**
   * Returns an observable that polls the given download id for its status every second, as the
   * manager doesn't have any notification system. It'll stop once the download finishes.
   *
   * @param id The id of the download to poll.
   */
  private fun pollStatus(id: Long): Observable<InstallStep> {
    val query = DownloadManager.Query().setFilterById(id)

    return Observable.interval(0, 1, TimeUnit.SECONDS)
      // Get the current download status
      .map {
        downloadManager.query(query).use { cursor ->
          cursor.moveToFirst()
          cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        }
      }
      // Ignore duplicate results
      .distinctUntilChanged()
      // Stop polling when the download fails or finishes
      .takeUntil { it == DownloadManager.STATUS_SUCCESSFUL || it == DownloadManager.STATUS_FAILED }
      // Map to our model
      .flatMap { status ->
        when (status) {
          DownloadManager.STATUS_PENDING -> Observable.just(InstallStep.Pending)
          DownloadManager.STATUS_RUNNING -> Observable.just(InstallStep.Downloading)
          else -> Observable.empty()
        }
      }
  }

  /**
   * Starts an intent to install the extension with the given uri.
   *
   * @param uri The uri of the extension to install.
   */
  fun installApk(downloadId: Long, uri: Uri) {
    val intent = Intent(context, CatalogInstallActivity::class.java)
      .setDataAndType(uri, APK_MIME)
      .putExtra(EXTRA_OPERATION, OPERATION_INSTALL)
      .putExtra(EXTRA_ID, downloadId)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)

    context.startActivity(intent)
  }

  /**
   * Starts an intent to uninstall the extension by the given package name.
   *
   * @param pkgName The package name of the extension to uninstall
   */
  fun uninstallApk(pkgName: String) = Completable.defer {
    val uninstallId = uninstallIds.incrementAndGet()

    val packageUri = Uri.parse("package:$pkgName")
    val intent = Intent(context, CatalogInstallActivity::class.java)
      .setData(packageUri)
      .putExtra(EXTRA_OPERATION, OPERATION_UNINSTALL)
      .putExtra(EXTRA_ID, uninstallId)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent
        .FLAG_ACTIVITY_NO_ANIMATION)

    val subject = CompletableSubject.create()
    activeUninstalls[uninstallId] = subject
    context.startActivity(intent)

    subject.timeout(3, TimeUnit.MINUTES)
      .doFinally { activeUninstalls.remove(uninstallId) }
  }

  /**
   * Sets the result of the installation of an extension.
   *
   * @param downloadId The id of the download.
   * @param result Whether the extension was installed or not.
   */
  fun setInstallResult(downloadId: Long, result: Boolean) {
    val step = if (result) InstallStep.Installed else InstallStep.Error
    downloadsRelay.accept(downloadId to step)
  }

  /**
   * Sets the result of the uninstallation of an extension.
   *
   * @param uninstallId The id of the uninstall process.
   * @param result Whether the extension was uninstalled or not.
   */
  fun setUninstallResult(uninstallId: Long, result: Boolean) {
    val subject = activeUninstalls[uninstallId] ?: return
    if (result) {
      subject.onComplete()
    } else {
      subject.onError(Exception())
    }
  }

  /**
   * Deletes the download for the given package name.
   *
   * @param pkgName The package name of the download to delete.
   */
  fun deleteDownload(pkgName: String) {
    val downloadId = activeDownloads.remove(pkgName)
    if (downloadId != null) {
      downloadManager.remove(downloadId)
    }
    if (activeDownloads.isEmpty()) {
      downloadReceiver.unregister()
    }
  }

  /**
   * Receiver that listens to download status events.
   */
  private inner class DownloadCompletionReceiver : BroadcastReceiver() {

    /**
     * Whether this receiver is currently registered.
     */
    private var isRegistered = false

    /**
     * Registers this receiver if it's not already.
     */
    fun register() {
      if (isRegistered) return
      isRegistered = true

      val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
      context.registerReceiver(this, filter)
    }

    /**
     * Unregisters this receiver if it's not already.
     */
    fun unregister() {
      if (!isRegistered) return
      isRegistered = false

      context.unregisterReceiver(this)
    }

    /**
     * Called when a download event is received. It looks for the download in the current active
     * downloads and notifies its installation step.
     */
    override fun onReceive(context: Context, intent: Intent?) {
      val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0) ?: return

      // Avoid events for downloads we didn't request
      if (id !in activeDownloads.values) return

      val uri = downloadManager.getUriForDownloadedFile(id)

      // Set next installation step
      if (uri != null) {
        downloadsRelay.accept(id to InstallStep.Installing)
      } else {
        Timber.error { "Couldn't locate downloaded APK" }
        downloadsRelay.accept(id to InstallStep.Error)
        return
      }

      // Due to a bug in Android versions prior to N, the installer can't open files that do
      // not contain the extension in the path, even if you specify the correct MIME.
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        val query = DownloadManager.Query().setFilterById(id)
        downloadManager.query(query).use { cursor ->
          if (cursor.moveToFirst()) {
            @Suppress("DEPRECATION")
            val uriCompat = File(cursor.getString(cursor.getColumnIndex(
              DownloadManager.COLUMN_LOCAL_FILENAME))).getUriCompat(context)
            installApk(id, uriCompat)
          }
        }
      } else {
        installApk(id, uri)
      }
    }
  }

  companion object {
    const val APK_MIME = "application/vnd.android.package-archive"
    const val EXTRA_ID = "CatalogInstaller.extra.ID"
    const val EXTRA_OPERATION = "CatalogInstaller.extra.OPERATION"

    const val OPERATION_INSTALL = 1
    const val OPERATION_UNINSTALL = 2
  }

}
