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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import tachiyomi.core.util.getUriCompat
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import timber.log.Timber
import timber.log.error
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlin.coroutines.resume

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
   * Map containing download id and a completable future that is called when the apk is installed.
   */
  private val pendingInstalls = hashMapOf<Long, CompletableDeferred<Boolean>>()

  /**
   * Map containing download id and a completable future that is called when the apk is uninstalled.
   */
  private val pendingUninstalls = hashMapOf<Long, CompletableDeferred<Boolean>>()

  /**
   * Counter used to generate unique uninstall ids. Used in [pendingUninstalls].
   */
  private val uninstallIds = AtomicLong()

  /**
   * Adds the given extension to the downloads queue and returns an observable containing its
   * step in the installation process.
   *
   * @param catalog The catalog to install.
   */
  fun downloadAndInstall(catalog: CatalogRemote) = flow {
    val request = DownloadManager.Request(Uri.parse(catalog.apkUrl))
      .setTitle(catalog.name)
      .setMimeType(APK_MIME)
      .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

    val downloadId = downloadManager.enqueue(request)

    try {
      emit(InstallStep.Pending)
      val uri = awaitDownloadUri(downloadId)
      if (uri == null) {
        emit(InstallStep.Error)
        return@flow
      }

      emit(InstallStep.Installing)
      val installed = installApk(downloadId, uri)

      emit(if (installed) InstallStep.Installed else InstallStep.Error)
    } finally {
      downloadManager.remove(downloadId)
    }
  }

  /**
   * Starts an intent to install the extension with the given uri.
   *
   * @param uri The uri of the extension to install.
   */
  private suspend fun installApk(downloadId: Long, uri: Uri): Boolean {
    val intent = Intent(context, CatalogInstallActivity::class.java)
      .setDataAndType(uri, APK_MIME)
      .putExtra(EXTRA_OPERATION, OPERATION_INSTALL)
      .putExtra(EXTRA_ID, downloadId)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)

    val future = CompletableDeferred<Boolean>()
    pendingInstalls[downloadId] = future
    context.startActivity(intent)

    return try {
      withTimeout(TimeUnit.MINUTES.toMillis(3)) {
        future.await()
      }
    } catch (e: TimeoutCancellationException) {
      false
    } finally {
      pendingInstalls.remove(downloadId)
    }
  }

  /**
   * Starts an intent to uninstall the extension by the given package name.
   *
   * @param pkgName The package name of the extension to uninstall
   */
  suspend fun uninstallApk(pkgName: String): Boolean {
    val uninstallId = uninstallIds.incrementAndGet()

    val packageUri = Uri.parse("package:$pkgName")
    val intent = Intent(context, CatalogInstallActivity::class.java)
      .setData(packageUri)
      .putExtra(EXTRA_OPERATION, OPERATION_UNINSTALL)
      .putExtra(EXTRA_ID, uninstallId)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent
        .FLAG_ACTIVITY_NO_ANIMATION)

    val future = CompletableDeferred<Boolean>()
    pendingUninstalls[uninstallId] = future
    context.startActivity(intent)

    return try {
      withTimeout(TimeUnit.MINUTES.toMillis(3)) {
        future.await()
      }
    } catch (e: TimeoutCancellationException) {
      false
    } finally {
      pendingUninstalls.remove(uninstallId)
    }
  }

  /**
   * Sets the result of the installation of an extension.
   *
   * @param downloadId The id of the download.
   * @param result Whether the extension was installed or not.
   */
  fun setInstallResult(downloadId: Long, result: Boolean) {
    val future = pendingInstalls[downloadId] ?: return
    future.complete(result)
  }

  /**
   * Sets the result of the uninstallation of an extension.
   *
   * @param uninstallId The id of the uninstall process.
   * @param result Whether the extension was uninstalled or not.
   */
  fun setUninstallResult(uninstallId: Long, result: Boolean) {
    val future = pendingUninstalls[uninstallId] ?: return
    future.complete(result)
  }

  private suspend fun awaitDownloadUri(downloadId: Long): Uri? {
    return try {
      withTimeout(TimeUnit.MINUTES.toMillis(3)) {
        suspendCancellableCoroutine<Uri?> {
          val receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent?) {
              val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0) ?: return
              if (id != downloadId) return

              val uri = downloadManager.getUriForDownloadedFile(id)

              // Set next installation step
              if (uri == null) {
                Timber.error { "Couldn't locate downloaded APK" }
                context.unregisterReceiver(this)
                it.resume(null)
                return
              }

              // Due to a bug in Android versions prior to N, the installer can't open files that do
              // not contain the extension in the path, even if you specify the correct MIME.
              val uriCompat = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                val query = DownloadManager.Query().setFilterById(id)
                downloadManager.query(query).use { cursor ->
                  if (cursor.moveToFirst()) {
                    @Suppress("DEPRECATION")
                    File(cursor.getString(cursor.getColumnIndex(
                      DownloadManager.COLUMN_LOCAL_FILENAME))).getUriCompat(context)
                  } else {
                    null
                  }
                }
              } else {
                uri
              }
              context.unregisterReceiver(this)
              it.resume(uriCompat)
            }

          }
          context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
          it.invokeOnCancellation {
            context.unregisterReceiver(receiver)
          }
        }
      }
    } catch (e: TimeoutCancellationException) {
      null
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
