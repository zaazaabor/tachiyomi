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
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.data.catalog.api.CatalogGithubApi
import tachiyomi.data.catalog.installer.CatalogInstallReceiver
import tachiyomi.data.catalog.installer.CatalogInstaller
import tachiyomi.data.catalog.installer.CatalogLoader
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogInternal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.CatalogSource
import javax.inject.Inject

class CatalogRepositoryImpl @Inject internal constructor(
  private val context: Application,
  private val loader: CatalogLoader,
  private val installer: CatalogInstaller,
  private val api: CatalogGithubApi,
  private val schedulers: RxSchedulers
) : CatalogRepository {

  var builtInCatalogs = emptyList<CatalogInternal>()
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

  private val remoteCatalogsRelay = BehaviorRelay.createDefault<List<CatalogRemote>>(emptyList())

  var remoteCatalogs = emptyList<CatalogRemote>()
    private set(value) {
      field = value
      remoteCatalogsRelay.accept(value)
    }

  /**
   * The source manager where the sources of the catalogs are added.
   */
  private lateinit var sourceManager: SourceManager

  /**
   * Loads and registers the installed catalogues.
   */
  fun init(sourceManager: SourceManager) {
    if (this::sourceManager.isInitialized) return

    this.sourceManager = sourceManager

    builtInCatalogs = sourceManager.getSources().filterIsInstance<CatalogSource>()
      .map { CatalogInternal(it.name, it) }

    installedCatalogs = loader.loadExtensions()
      .filterIsInstance<CatalogLoader.Result.Success>()
      .map { it.catalog }
      .onEach { sourceManager.registerSource(it.source) }

    // TODO
    //CatalogInstallReceiver(InstallationListener()).register(context)
  }

  override fun getInternalCatalogsFlowable(): Flowable<List<CatalogInternal>> {
    return Flowable.just(builtInCatalogs)
  }

  override fun getInstalledCatalogsFlowable(): Flowable<List<CatalogInstalled>> {
    return installedCatalogsRelay.toFlowable(BackpressureStrategy.LATEST)
  }

  // TODO local DB persistence
  override fun getRemoteCatalogsFlowable(): Flowable<List<CatalogRemote>> {
    if (remoteCatalogs.isEmpty()) {
      refreshAvailableCatalogs()
    }
    return remoteCatalogsRelay.toFlowable(BackpressureStrategy.LATEST)
  }

  fun refreshAvailableCatalogs() {
    api.findCatalogs()
      .subscribeOn(schedulers.io)
      .doOnSuccess { remoteCatalogs = it }
      .ignoreElement()
      .onErrorComplete()
      .subscribe()
  }

  /**
   * TODO
   * Listener which receives events of the extensions being installed, updated or removed.
   */
  private inner class InstallationListener : CatalogInstallReceiver.Listener {

    override fun onCatalogInstalled(catalog: CatalogInstalled) {
    }

    override fun onCatalogUpdated(catalog: CatalogInstalled) {
    }

    override fun onPackageUninstalled(pkgName: String) {
    }

  }

}
