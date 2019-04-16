/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.repository

import android.app.Application
import android.os.SystemClock
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.Query
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import tachiyomi.core.db.inTransaction
import tachiyomi.core.rx.CoroutineDispatchers
import tachiyomi.core.rx.asFlow
import tachiyomi.data.BuildConfig
import tachiyomi.data.catalog.api.CatalogGithubApi
import tachiyomi.data.catalog.installer.CatalogInstallReceiver
import tachiyomi.data.catalog.installer.CatalogInstaller
import tachiyomi.data.catalog.installer.CatalogLoader
import tachiyomi.data.catalog.sql.CatalogTable
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogInternal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.TestSource
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class CatalogRepositoryImpl @Inject constructor(
  private val context: Application,
  private val storio: StorIOSQLite,
  private val loader: CatalogLoader,
  private val installer: CatalogInstaller,
  private val api: CatalogGithubApi,
  private val dispatchers: CoroutineDispatchers
) : CatalogRepository {

  var internalCatalogs = emptyList<CatalogInternal>()
    private set

  /**
   * List of the currently installed catalogs.
   */
  override var installedCatalogs = emptyList<CatalogInstalled>()
    private set(value) {
      field = value
      installedCatalogsChannel.offer(value)
    }

  /**
   * Relay used to notify the installed catalogs.
   */
  private val installedCatalogsChannel = ConflatedBroadcastChannel(installedCatalogs)

  var remoteCatalogs = emptyList<CatalogRemote>()
    private set(value) {
      field = value
      remoteCatalogsChannel.offer(value)
      setUpdateFieldOfInstalledCatalogs(value)
    }

  private val remoteCatalogsChannel = ConflatedBroadcastChannel(remoteCatalogs)

  private var lastTimeApiChecked: Long? = null

  private var minTimeApiCheck = TimeUnit.MINUTES.toMillis(5)

  /**
   * The source manager where the sources of the catalogs are added.
   */
  private lateinit var sourceManager: SourceManager

  init {
    initRemoteCatalogs()
  }

  /**
   * Loads and registers the installed catalogues.
   */
  fun init(sourceManager: SourceManager) {
    if (this::sourceManager.isInitialized) return

    this.sourceManager = sourceManager

    internalCatalogs = initInternalCatalogs()
      .onEach { sourceManager.registerSource(it.source) }

    installedCatalogs = loader.loadExtensions()
      .filterIsInstance<CatalogLoader.Result.Success>()
      .map { it.catalog }
      .onEach { sourceManager.registerSource(it.source) }

    CatalogInstallReceiver(InstallationListener(), loader, dispatchers).register(context)
  }

  override fun getInternalCatalogsFlow(): Flow<List<CatalogInternal>> {
    return flowOf(internalCatalogs)
  }

  override fun getInstalledCatalogsFlow(): Flow<List<CatalogInstalled>> {
    return installedCatalogsChannel.asFlow()
  }

  override fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>> {
    return remoteCatalogsChannel.asFlow()
  }

  private fun initInternalCatalogs(): List<CatalogInternal> {
    val catalogs = mutableListOf<CatalogInternal>()
    if (BuildConfig.DEBUG) {
      catalogs.add(CatalogInternal(TestSource(), "Source used for testing"))
    }
    return catalogs
  }

  private fun initRemoteCatalogs() {
    storio.get()
      .listOfObjects(CatalogRemote::class.java)
      .withQuery(Query.builder()
        .table(CatalogTable.TABLE)
        .orderBy("${CatalogTable.COL_LANG}, ${CatalogTable.COL_NAME}")
        .build())
      .prepare()
      .asRxSingle()
      .doOnSuccess {
        remoteCatalogs = it
        GlobalScope.launch(dispatchers.io) { refreshRemoteCatalogs(false) }
      }
      .ignoreElement()
      .onErrorComplete()
      .subscribe()
  }

  override suspend fun refreshRemoteCatalogs(forceRefresh: Boolean) {
    val lastCheck = lastTimeApiChecked
    if (!forceRefresh && lastCheck != null
      && lastCheck - SystemClock.elapsedRealtime() < minTimeApiCheck) {
      return
    }
    lastTimeApiChecked = SystemClock.elapsedRealtime()

    api.findCatalogs()
      .doOnSuccess { newCatalogs ->
        storio.inTransaction {
          storio.delete()
            .byQuery(DeleteQuery.builder().table(CatalogTable.TABLE).build())
            .prepare()
            .executeAsBlocking()

          storio.put()
            .objects(newCatalogs)
            .prepare()
            .executeAsBlocking()
        }

        remoteCatalogs = newCatalogs
      }
      .ignoreElement()
      .await()
  }

  /**
   * Sets the update field of the installed extensions with the given [remoteCatalogs].
   *
   * @param remoteCatalogs The list of catalogs given by the [api].
   */
  private fun setUpdateFieldOfInstalledCatalogs(remoteCatalogs: List<CatalogRemote>) {
    val mutInstalledCatalogs = installedCatalogs.toMutableList()
    var changed = false

    for ((index, installedCatalog) in mutInstalledCatalogs.withIndex()) {
      val pkgName = installedCatalog.pkgName
      val remoteCatalog = remoteCatalogs.find { it.pkgName == pkgName } ?: continue

      val hasUpdate = remoteCatalog.versionCode > installedCatalog.versionCode
      if (installedCatalog.hasUpdate != hasUpdate) {
        mutInstalledCatalogs[index] = installedCatalog.copy(hasUpdate = hasUpdate)
        changed = true
      }
    }
    if (changed) {
      installedCatalogs = mutInstalledCatalogs
    }
  }

  override fun installCatalog(catalog: CatalogRemote): Flow<InstallStep> {
    return installer.downloadAndInstall(catalog).asFlow()
  }

  override suspend fun uninstallCatalog(catalog: CatalogInstalled) {
    return installer.uninstallApk(catalog.pkgName).await()
  }

  /**
   * Listener which receives events of the catalogs being installed, updated or removed.
   */
  private inner class InstallationListener : CatalogInstallReceiver.Listener {

    @Synchronized
    override fun onCatalogInstalled(catalog: CatalogInstalled) {
      installedCatalogs = installedCatalogs + catalog.withUpdateCheck()
      sourceManager.registerSource(catalog.source)
    }

    @Synchronized
    override fun onCatalogUpdated(catalog: CatalogInstalled) {
      val mutInstalledCatalogs = installedCatalogs.toMutableList()
      val oldCatalog = mutInstalledCatalogs.find { it.pkgName == catalog.pkgName }
      if (oldCatalog != null) {
        mutInstalledCatalogs -= oldCatalog
        sourceManager.unregisterSource(catalog.source)
      }
      mutInstalledCatalogs += catalog.withUpdateCheck()
      installedCatalogs = mutInstalledCatalogs
      sourceManager.registerSource(catalog.source)
    }

    @Synchronized
    override fun onPackageUninstalled(pkgName: String) {
      val installedCatalog = installedCatalogs.find { it.pkgName == pkgName }
      if (installedCatalog != null) {
        installedCatalogs = installedCatalogs - installedCatalog
        sourceManager.unregisterSource(installedCatalog.source)
      }
    }

    private fun CatalogInstalled.withUpdateCheck(): CatalogInstalled {
      val remoteCatalog = remoteCatalogs.find { it.pkgName == pkgName }
      if (remoteCatalog != null && remoteCatalog.versionCode > versionCode) {
        return copy(hasUpdate = true)
      }
      return this
    }

  }

}
