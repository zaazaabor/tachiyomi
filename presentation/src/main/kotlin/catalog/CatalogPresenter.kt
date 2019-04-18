/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalog

import com.freeletics.coredux.SideEffect
import com.freeletics.coredux.createStore
import com.jakewharton.rxrelay2.BehaviorRelay
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tachiyomi.core.stdlib.debounce
import tachiyomi.domain.catalog.interactor.GetCatalogs
import tachiyomi.domain.catalog.interactor.InstallCatalog
import tachiyomi.domain.catalog.interactor.RefreshRemoteCatalogs
import tachiyomi.domain.catalog.interactor.UpdateCatalog
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.ui.presenter.BasePresenter
import javax.inject.Inject

class CatalogsPresenter @Inject constructor(
  private val getCatalogs: GetCatalogs,
  private val installCatalog: InstallCatalog,
  private val updateCatalog: UpdateCatalog,
  private val refreshRemoteCatalogs: RefreshRemoteCatalogs
) : BasePresenter() {

  val state = BehaviorRelay.create<ViewState>()

  private val store = scope.createStore(
    name = "Catalog presenter",
    initialState = getInitialViewState(),
    sideEffects = getSideEffects(),
    logSinks = getLogSinks(),
    reducer = { state, action -> action.reduce(state) }
  )

  init {
    store.subscribeToChangedStateUpdatesInMain { state.accept(it) }
    store.dispatch(Action.Init)
  }

  private fun getInitialViewState(): ViewState {
    return ViewState()
  }

  private fun getSideEffects(): List<SideEffect<ViewState, Action>> {
    val sideEffects = mutableListOf<SideEffect<ViewState, Action>>()

    sideEffects += FlowSideEffect("Subscribe to catalogs") { stateFn, action, _, handler ->
      when (action) {
        is Action.Init, is Action.SetLanguageChoice -> handler {
          val choice = stateFn().languageChoice
          getCatalogs.subscribe(excludeRemoteInstalled = true).map { (local, remote) ->
            val items = mutableListOf<Any>()

            if (local.isNotEmpty()) {
              items.add(CatalogHeader.Installed)

              val (updatable, upToDate) = local.partition { it is CatalogInstalled && it.hasUpdate }
              when {
                updatable.isEmpty() -> {
                  items.addAll(upToDate)
                }
                upToDate.isEmpty() -> {
                  items.add(CatalogSubheader.UpdateAvailable(updatable.size))
                  items.addAll(updatable)
                }
                else -> {
                  items.add(CatalogSubheader.UpdateAvailable(updatable.size))
                  items.addAll(updatable)
                  items.add(CatalogSubheader.UpToDate)
                  items.addAll(upToDate)
                }
              }
            }

            if (remote.isNotEmpty()) {
              val choices = LanguageChoices(getLanguageChoices(remote, local), choice)
              val availableCatalogsFiltered = getRemoteCatalogsForLanguageChoice(remote, choice)

              items.add(CatalogHeader.Available)
              items.add(choices)
              items.addAll(availableCatalogsFiltered)
            }

            Action.ItemsUpdate(items)
          }
        }
        else -> null
      }
    }

    sideEffects += MultiFlowSideEffect("Install catalog") { _, action, _, handler ->
      when (action) {
        is Action.InstallCatalog -> handler {
          val catalog = action.catalog
          installCatalog.await(catalog).map { Action.InstallStepUpdate(catalog.pkgName, it) }
        }
        is Action.UpdateCatalog -> handler {
          val catalog = action.catalog
          updateCatalog.await(catalog).map { Action.InstallStepUpdate(catalog.pkgName, it) }
        }
        else -> null
      }
    }

    sideEffects += FlowSideEffect("Refresh catalogs") { _, action, _, handler ->
      when (action) {
        Action.Init, is Action.RefreshCatalogs -> handler {
          val force = if (action is Action.RefreshCatalogs) action.force else false

          // TODO there should be a better way to do this
          val deferred = scope.async { refreshRemoteCatalogs.await(force) }
          flow {
            emit(Action.RefreshingCatalogs(true))
            runCatching { deferred.await() }
            emit(Action.RefreshingCatalogs(false))
          }
            // Debounce for a frame. Sometimes this operation returns immediately, so with this
            // we avoid showing the progress bar if not really needed
            .debounce(16)
        }
        else -> null
      }
    }

    return sideEffects
  }

  private fun getLanguageChoices(
    remote: List<CatalogRemote>,
    local: List<CatalogLocal>
  ): List<LanguageChoice> {
    val knownLanguages = mutableListOf<LanguageChoice.One>()
    val unknownLanguages = mutableListOf<Language>()

    val languageComparators = UserLanguagesComparator()
      .then(InstalledLanguagesComparator(local))
      .thenBy { it.code }

    remote.asSequence()
      .map { Language(it.lang) }
      .distinct()
      .sortedWith(languageComparators)
      .forEach { code ->
        if (code.toEmoji() != null) {
          knownLanguages.add(LanguageChoice.One(code))
        } else {
          unknownLanguages.add(code)
        }
      }

    val languages = mutableListOf<LanguageChoice>()
    languages.add(LanguageChoice.All)
    languages.addAll(knownLanguages)
    if (unknownLanguages.isNotEmpty()) {
      languages.add(LanguageChoice.Others(unknownLanguages))
    }

    return languages
  }

  private fun getRemoteCatalogsForLanguageChoice(
    catalogs: List<CatalogRemote>,
    choice: LanguageChoice
  ): List<CatalogRemote> {
    return when (choice) {
      is LanguageChoice.All -> catalogs
      is LanguageChoice.One -> catalogs.filter { choice.language.code == it.lang }
      is LanguageChoice.Others -> {
        val codes = choice.languages.map { it.code }
        catalogs.filter { it.lang in codes }
      }
    }
  }

  fun setLanguageChoice(languageChoice: LanguageChoice) {
    store.dispatch(Action.SetLanguageChoice(languageChoice))
  }

  fun installCatalog(catalog: Catalog) {
    when (catalog) {
      is CatalogInstalled -> store.dispatch(Action.UpdateCatalog(catalog))
      is CatalogRemote -> store.dispatch(Action.InstallCatalog(catalog))
    }
  }

  fun refreshCatalogs() {
    store.dispatch(Action.RefreshCatalogs(true))
  }

}
