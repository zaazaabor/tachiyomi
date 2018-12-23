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
import tachiyomi.domain.catalog.model.Catalog
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

  var builtInCatalogs = emptyList<Catalog.Internal>()
    private set

  /**
   * Relay used to notify the installed catalogs.
   */
  private val installedCatalogsRelay = BehaviorRelay.create<List<Catalog.Installed>>()

  /**
   * List of the currently installed catalogs.
   */
  var installedCatalogs = emptyList<Catalog.Installed>()
    private set(value) {
      field = value
      installedCatalogsRelay.accept(value)
    }

  private val availableCatalogsRelay = BehaviorRelay.createDefault<List<Catalog.Available>>(emptyList())

  var availableCatalogs = emptyList<Catalog.Available>()
    private set(value) {
      field = value
      availableCatalogsRelay.accept(value)
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
      .map { Catalog.Internal(it.name, "", "1", 1, it) }

    installedCatalogs = loader.loadExtensions()
      .filterIsInstance<CatalogLoader.Result.Success>()
      .map { it.catalog }
      .onEach { sourceManager.registerSource(it.source) }

    // TODO
    //CatalogInstallReceiver(InstallationListener()).register(context)
  }

  override fun getInternalCatalogsFlowable(): Flowable<List<Catalog.Internal>> {
    return Flowable.just(builtInCatalogs)
  }

  override fun getInstalledCatalogsFlowable(): Flowable<List<Catalog.Installed>> {
    return installedCatalogsRelay.toFlowable(BackpressureStrategy.LATEST)
  }

  // TODO local DB persistence
  override fun getAvailableCatalogsFlowable(): Flowable<List<Catalog.Available>> {
    if (availableCatalogs.isEmpty()) {
      refreshAvailableCatalogs()
    }
    return availableCatalogsRelay.toFlowable(BackpressureStrategy.LATEST)
  }

  fun refreshAvailableCatalogs() {
    api.findCatalogs()
      .subscribeOn(schedulers.io)
      .doOnSuccess { availableCatalogs = it }
      .ignoreElement()
      .onErrorComplete()
      .subscribe()
  }

  /**
   * TODO
   * Listener which receives events of the extensions being installed, updated or removed.
   */
  private inner class InstallationListener : CatalogInstallReceiver.Listener {

    override fun onCatalogInstalled(catalog: Catalog.Installed) {
    }

    override fun onCatalogUpdated(catalog: Catalog.Installed) {
    }

    override fun onPackageUninstalled(pkgName: String) {
    }

  }

}
