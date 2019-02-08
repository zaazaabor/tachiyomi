/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalog

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.domain.catalog.interactor.InstallCatalog
import tachiyomi.domain.catalog.interactor.RefreshRemoteCatalogs
import tachiyomi.domain.catalog.interactor.SubscribeCatalogs
import tachiyomi.domain.catalog.interactor.UpdateCatalog
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import tachiyomi.ui.presenter.BasePresenter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CatalogsPresenter @Inject constructor(
  private val subscribeCatalogs: SubscribeCatalogs,
  private val installCatalog: InstallCatalog,
  private val updateCatalog: UpdateCatalog,
  private val refreshRemoteCatalogs: RefreshRemoteCatalogs,
  private val schedulers: RxSchedulers
) : BasePresenter() {

  private val actions = PublishRelay.create<Action>()

  val state = BehaviorRelay.create<ViewState>()

  init {
    actions
      .observeOn(schedulers.io)
      .reduxStore(
        initialState = ViewState(),
        sideEffects = listOf(
          ::loadCatalogsSideEffect,
          ::installCatalogSideEffect,
          ::refreshRemoteCatalogsSideEffect
        ),
        reducer = { state, action -> action.reduce(state) }
      )
      .distinctUntilChanged()
      .logOnNext()
      .observeOn(schedulers.main)
      .subscribe(state::accept)
      .addTo(disposables)
  }

  @Suppress("unused_parameter")
  private fun loadCatalogsSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    val catalogs = subscribeCatalogs.interact(excludeRemoteInstalled = true)
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.io)

    val selectedLanguage = actions.ofType<Action.SetLanguageChoice>()
      .observeOn(schedulers.io)
      .map { it.choice }
      .startWith(stateFn().languageChoice)

    return Observables.combineLatest(
      catalogs,
      selectedLanguage
    ) { (local, remote), choice ->
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

      items
    }.map(Action::ItemsUpdate)
  }

  @Suppress("unused_parameter")
  private fun installCatalogSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    val installCatalogsObservable = actions.ofType<Action.InstallCatalog>()
      .flatMap { action ->
        installCatalog.interact(action.catalog).map { action.catalog.pkgName to it }
      }

    val updateCatalogsObservable = actions.ofType<Action.UpdateCatalog>()
      .flatMap { action ->
        updateCatalog.interact(action.catalog).map { action.catalog.pkgName to it }
      }

    return Observable.merge(installCatalogsObservable, updateCatalogsObservable)
      .scan(emptyMap<String, InstallStep>()) { state, pkgAndStep ->
        val (pkgName, step) = pkgAndStep
        if (step == InstallStep.Installed) {
          state - pkgName
        } else {
          state + pkgAndStep
        }
      }
      .map(Action::InstallingCatalogsUpdate)
  }

  private fun refreshRemoteCatalogsSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<ViewState>
  ): Observable<Action> {
    return actions.ofType<Action.RefreshCatalogs>()
      .startWith(Action.RefreshCatalogs(false))
      .observeOn(schedulers.io)
      .flatMap {
        refreshRemoteCatalogs.interact(it.force)
          .onErrorComplete()
          .andThen(Observable.just(Action.RefreshingCatalogs(false)))
          .startWith(Action.RefreshingCatalogs(true))
          // Debounce for a frame. Sometimes this completable returns immediately, so with this
          // we avoid showing the progress bar if not really needed
          .debounce(16, TimeUnit.MILLISECONDS)
      }
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
    actions.accept(Action.SetLanguageChoice(languageChoice))
  }

  fun installCatalog(catalog: Catalog) {
    when (catalog) {
      is CatalogInstalled -> actions.accept(Action.UpdateCatalog(catalog))
      is CatalogRemote -> actions.accept(Action.InstallCatalog(catalog))
    }
  }

  fun refreshCatalogs() {
    actions.accept(Action.RefreshCatalogs(true))
  }

}
