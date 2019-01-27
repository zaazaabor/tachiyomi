/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.installer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tachiyomi.core.di.AppScope
import tachiyomi.core.util.toast

/**
 * Activity used to install or uninstall extensions, because we can only receive the result of the
 * installation with [startActivityForResult], which we need to update the UI.
 */
class CatalogInstallActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val action = intent.getIntExtra(CatalogInstaller.EXTRA_OPERATION, 0)
    when (action) {
      CatalogInstaller.OPERATION_INSTALL -> installCatalog()
      CatalogInstaller.OPERATION_UNINSTALL -> uninstallCatalog()
      else -> finish()
    }
  }

  private fun installCatalog() {
    val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE)
      .setDataAndType(intent.data, intent.type)
      .putExtra(Intent.EXTRA_RETURN_RESULT, true)
      .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    try {
      startActivityForResult(installIntent, INSTALL_REQUEST_CODE)
    } catch (error: Exception) {
      // Either installer package can't be found (probably bots) or there's a security exception
      // with the download manager. Nothing we can workaround.
      toast(error.message)
      finish()
    }
  }

  private fun uninstallCatalog() {
    val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
      .setDataAndType(intent.data, intent.type)
      .putExtra(Intent.EXTRA_RETURN_RESULT, true)
      .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    try {
      startActivityForResult(uninstallIntent, UNINSTALL_REQUEST_CODE)
    } catch (error: Exception) {
      // Either uninstaller package can't be found (probably bots) or there's a security exception
      // with the download manager. Nothing we can workaround.
      toast(error.message)
      finish()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      INSTALL_REQUEST_CODE -> checkInstallationResult(resultCode)
      UNINSTALL_REQUEST_CODE -> checkUninstallationResult(resultCode)
    }
    finish()
  }

  private fun checkInstallationResult(resultCode: Int) {
    val downloadId = intent?.extras?.getLong(CatalogInstaller.EXTRA_ID) ?: return
    val success = resultCode == RESULT_OK

    val installer = AppScope.getInstance<CatalogInstaller>()
    installer.setInstallResult(downloadId, success)
  }

  private fun checkUninstallationResult(resultCode: Int) {
    val uninstallId = intent?.extras?.getLong(CatalogInstaller.EXTRA_ID) ?: return
    val success = resultCode == RESULT_OK

    val installer = AppScope.getInstance<CatalogInstaller>()
    installer.setUninstallResult(uninstallId, success)
  }

  private companion object {
    const val INSTALL_REQUEST_CODE = 500
    const val UNINSTALL_REQUEST_CODE = 501
  }

}
