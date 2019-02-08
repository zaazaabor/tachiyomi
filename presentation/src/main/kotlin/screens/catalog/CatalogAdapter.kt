/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalog

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogInternal
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.domain.catalog.model.InstallStep
import tachiyomi.ui.adapter.BaseListAdapter
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.glide.GlideRequests

class CatalogAdapter(
  context: Context,
  private val listener: Listener,
  private val glideRequests: GlideRequests
) : BaseListAdapter<Any, BaseViewHolder>() {

  private val langsAdapter = CatalogLangsAdapter(context, listener)

  private val catalogTheme = CatalogHolder.Theme(context)

  private var oldInstalling = emptyMap<String, InstallStep>()
  private var newInstalling = emptyMap<String, InstallStep>()

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return when (item) {
      is Catalog -> VIEW_TYPE_CATALOG
      is LanguageChoices -> VIEW_TYPE_LANGUAGES
      is CatalogHeader -> VIEW_TYPE_HEADER
      is CatalogSubheader -> VIEW_TYPE_SUBHEADER
      else -> error("Unknown view type for item class ${item.javaClass}")
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    return when (viewType) {
      VIEW_TYPE_CATALOG -> CatalogHolder(parent, catalogTheme, glideRequests, this)
      VIEW_TYPE_LANGUAGES -> CatalogLangsHolder(parent, langsAdapter)
      VIEW_TYPE_HEADER -> CatalogHeaderHolder(parent)
      VIEW_TYPE_SUBHEADER -> CatalogSubheaderHolder(parent)
      else -> error("Unknown view type $viewType")
    }
  }

  override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: List<Any>) {
    if (payloads.isEmpty()) {
      onBindViewHolder(holder, position)
      return
    }

    val item = getItem(position)
    if (holder is CatalogHolder && Payload.Install in payloads) {
      when (item) {
        is CatalogInstalled ->
          holder.bindInstallButton(item, newInstalling[item.pkgName])
        is CatalogRemote ->
          holder.bindInstallButton(item, newInstalling[item.pkgName])
      }
    }
  }

  override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
    val item = getItem(position)
    when (item) {
      is CatalogInternal -> (holder as CatalogHolder).bind(item)
      is CatalogInstalled -> {
        (holder as CatalogHolder).bind(item, newInstalling[item.pkgName])
      }
      is CatalogRemote -> {
        (holder as CatalogHolder).bind(item, newInstalling[item.pkgName])
      }
      is CatalogHeader -> (holder as CatalogHeaderHolder).bind(item)
      is CatalogSubheader -> (holder as CatalogSubheaderHolder).bind(item)
    }
  }

  override fun onViewRecycled(holder: BaseViewHolder) {
    holder.recycle()
  }

  fun submitItems(
    items: List<Any>,
    installingCatalogs: Map<String, InstallStep>
  ) {
    this.oldInstalling = installingCatalogs
    submitList(items, forceSubmit = true)

    val choices = items.filterIsInstance<LanguageChoices>().firstOrNull()
    if (choices != null) {
      langsAdapter.submitChoices(choices)
    }
  }

  override fun onLatchList(newList: List<Any>) {
    newInstalling = oldInstalling
  }

  override val itemCallback = object : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any) = when (newItem) {
      // Consider same item if package name matches for improved animations
      is CatalogInstalled -> {
        (oldItem is CatalogInstalled && oldItem.pkgName == newItem.pkgName)
          || (oldItem is CatalogRemote && oldItem.pkgName == newItem.pkgName)
      }
      is CatalogRemote -> {
        (oldItem is CatalogRemote && oldItem.pkgName == newItem.pkgName)
          || (oldItem is CatalogInstalled && oldItem.pkgName == newItem.pkgName)
      }

      // Handled by child adapter
      is LanguageChoices -> oldItem is LanguageChoices

      else -> oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any) = when (newItem) {
      is CatalogInstalled -> {
        oldItem == newItem && oldInstalling[newItem.pkgName] == newInstalling[newItem.pkgName]
      }
      is CatalogRemote -> {
        oldItem == newItem && oldInstalling[newItem.pkgName] == newInstalling[newItem.pkgName]
      }
      else -> true
    }

    override fun getChangePayload(oldItem: Any, newItem: Any) = when (newItem) {
      is CatalogInstalled -> {
        if (oldItem == newItem
          && oldInstalling[newItem.pkgName] == newInstalling[newItem.pkgName]) {
          Payload.Install
        } else {
          null
        }
      }
      is CatalogRemote -> {
        if (oldItem == newItem
          && oldInstalling[newItem.pkgName] == newInstalling[newItem.pkgName]) {
          Payload.Install
        } else {
          null
        }
      }
      else -> null
    }
  }

  fun handleRowClick(position: Int) {
    val item = getItemOrNull(position) as? Catalog ?: return
    listener.onCatalogClick(item)
  }

  fun handleInstallClick(position: Int) {
    val item = getItemOrNull(position) as? Catalog ?: return
    listener.onInstallClick(item)
  }

  fun handleSettingsClick(position: Int) {
    val item = getItemOrNull(position) as? Catalog ?: return
    listener.onSettingsClick(item)
  }

  interface Listener {
    fun onCatalogClick(catalog: Catalog)
    fun onLanguageChoiceClick(languageChoice: LanguageChoice)
    fun onInstallClick(catalog: Catalog)
    fun onSettingsClick(catalog: Catalog)
  }

  sealed class Payload {
    object Install : Payload()
  }

  companion object {
    const val VIEW_TYPE_CATALOG = 1
    const val VIEW_TYPE_LANGUAGES = 2
    const val VIEW_TYPE_HEADER = 3
    const val VIEW_TYPE_SUBHEADER = 4
  }

}
