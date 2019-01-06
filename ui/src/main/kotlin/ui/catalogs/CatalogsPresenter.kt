/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.addTo
import tachiyomi.domain.catalog.interactor.GetLocalCatalogs
import tachiyomi.domain.catalog.interactor.GetRemoteCatalogs
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.ui.base.BasePresenter
import javax.inject.Inject

class CatalogsPresenter @Inject constructor(
  private val getLocalCatalogs: GetLocalCatalogs,
  private val getRemoteCatalogs: GetRemoteCatalogs,
  private val schedulers: RxSchedulers
) : BasePresenter() {

  private val actions = PublishRelay.create<Action>()

  val state = BehaviorRelay.create<CatalogsViewState>()

  init {
    actions
      .observeOn(schedulers.io)
      .reduxStore(
        initialState = CatalogsViewState(),
        sideEffects = listOf(::loadCatalogsSideEffect),
        reducer = ::reduce
      )
      .distinctUntilChanged()
      .observeOn(schedulers.main)
      .subscribe(state::accept)
      .addTo(disposables)
  }

  @Suppress("unused_parameter")
  private fun loadCatalogsSideEffect(
    actions: Observable<Action>,
    stateFn: StateAccessor<CatalogsViewState>
  ): Observable<Action> {
    val localCatalogs = getLocalCatalogs.interact()
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.io)
      .toObservable()

    val remoteCatalogs = getRemoteCatalogs.interact()
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.io)
      .toObservable()

    val selectedLanguage = actions.ofType<Action.SetLanguageChoice>()
      .observeOn(schedulers.io)
      .map { it.choice }
      .startWith(stateFn().languageChoice)

    return Observables.combineLatest(
      localCatalogs,
      remoteCatalogs,
      selectedLanguage
    ) { local, remote, choice ->
      val items = mutableListOf<Any>()

      if (local.isNotEmpty()) {
        val updatable = local.count { it is CatalogInstalled && it.hasUpdate }
        items.add(CatalogHeader.Installed(updatable))
        items.addAll(local)
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

}

private sealed class Action {
  data class ItemsUpdate(val items: List<Any>) : Action()
  data class SetLanguageChoice(val choice: LanguageChoice) : Action()
}

private fun reduce(state: CatalogsViewState, action: Action): CatalogsViewState {
  return when (action) {
    is Action.ItemsUpdate -> state.copy(items = action.items)
    is Action.SetLanguageChoice -> state.copy(languageChoice = action.choice)
  }
}
