/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog

import android.app.Application
import com.jakewharton.rxrelay2.BehaviorRelay
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.Query
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import tachiyomi.core.db.inTransaction
import tachiyomi.core.rx.CoroutineDispatchers
import tachiyomi.data.catalog.api.CatalogGithubApi
import tachiyomi.data.catalog.installer.CatalogInstallReceiver
import tachiyomi.data.catalog.installer.CatalogInstaller
import tachiyomi.data.catalog.installer.CatalogLoader
import tachiyomi.data.catalog.sql.CatalogTable
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogInternal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.CatalogSource
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
   * Relay used to notify the installed catalogs.
   */
  private val installedCatalogsRelay = BehaviorRelay.create<List<CatalogInstalled>>()

  /**
   * List of the currently installed catalogs.
   */
  override var installedCatalogs = emptyList<CatalogInstalled>()
    private set(value) {
      field = value
      installedCatalogsRelay.accept(value)
    }

  private val remoteCatalogsRelay = BehaviorRelay.create<List<CatalogRemote>>()

  var remoteCatalogs = emptyList<CatalogRemote>()
    private set(value) {
      field = value
      remoteCatalogsRelay.accept(value)
    }

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

    internalCatalogs = sourceManager.getSources()
      .filterIsInstance<CatalogSource>()
      .map { CatalogInternal(it.name, it) }

    installedCatalogs = loader.loadExtensions()
      .filterIsInstance<CatalogLoader.Result.Success>()
      .map { it.catalog }
      .onEach { sourceManager.registerSource(it.source) }

    CatalogInstallReceiver(InstallationListener(), loader, dispatchers).register(context)
  }

  override fun getInternalCatalogsFlowable(): Flowable<List<CatalogInternal>> {
    return Flowable.just(internalCatalogs)
  }

  override fun getInstalledCatalogsFlowable(): Flowable<List<CatalogInstalled>> {
    return installedCatalogsRelay.toFlowable(BackpressureStrategy.LATEST)
  }

  override fun getRemoteCatalogsFlowable(): Flowable<List<CatalogRemote>> {
    return remoteCatalogsRelay.toFlowable(BackpressureStrategy.LATEST)
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
      .doOnSuccess { remoteCatalogs = it }
      .flatMapCompletable { refreshAvailableCatalogs() }
      .onErrorComplete()
      .subscribe()
  }

  fun refreshAvailableCatalogs(): Completable {
    return api.findCatalogs()
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
  }

  /**
   * Listener which receives events of the catalogs being installed, updated or removed.
   */
  private inner class InstallationListener : CatalogInstallReceiver.Listener {

    @Synchronized
    override fun onCatalogInstalled(catalog: CatalogInstalled) {
      installedCatalogs += catalog
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
      mutInstalledCatalogs += catalog
      installedCatalogs = mutInstalledCatalogs
      sourceManager.registerSource(catalog.source)
    }

    @Synchronized
    override fun onPackageUninstalled(pkgName: String) {
      val installedCatalog = installedCatalogs.find { it.pkgName == pkgName }
      if (installedCatalog != null) {
        installedCatalogs -= installedCatalog
        sourceManager.unregisterSource(installedCatalog.source)
      }
    }

  }

}
