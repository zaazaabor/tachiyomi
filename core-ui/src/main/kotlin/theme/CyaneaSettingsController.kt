/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.theme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceController
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceScreen
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import androidx.recyclerview.widget.RecyclerView
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.app.BaseCyaneaActivity
import com.jaredrummler.cyanea.prefs.CyaneaThemePickerActivity
import com.jaredrummler.cyanea.prefs.CyaneaThemePickerLauncher
import com.jaredrummler.cyanea.tinting.SystemBarTint
import com.jaredrummler.cyanea.utils.ColorUtils
import tachiyomi.core.ui.R

class CyaneaSettingsController : PreferenceController(),
  Preference.OnPreferenceChangeListener,
  Preference.OnPreferenceClickListener {

  private lateinit var prefThemePicker: Preference
  private lateinit var prefColorPrimary: ColorPreferenceCompat
  private lateinit var prefColorAccent: ColorPreferenceCompat
  private lateinit var prefColorBackground: ColorPreferenceCompat
  private lateinit var prefColorNavBar: SwitchPreferenceCompat

  /**
   * The [Cyanea] instance used for styling.
   */
  open val cyanea: Cyanea get() = (activity as? BaseCyaneaActivity)?.cyanea ?: Cyanea.instance

  /**
   * Get the preferences resource to load into the preference hierarchy.
   *
   * The preferences should contain a [ColorPreferenceCompat] for "pref_color_primary",
   * "pref_color_accent" and "pref_color_background".
   *
   * It should also contain preferences for "pref_theme_picker" and "pref_color_navigation_bar".
   *
   * @return The XML resource id to inflate
   */
  @XmlRes open fun getPreferenceXmlResId(): Int = R.xml.pref_cyanea

  /**
   * Sets whether to reserve the space of all Preference views. If set to false, all padding will be removed.
   *
   * By default, if the action bar is displaying home as up then padding will be added to the preference.
   */
  open val iconSpaceReserved = false
//    get() = (activity as? AppCompatActivity)?.supportActionBar?.displayOptions?.and(ActionBar.DISPLAY_HOME_AS_UP) != 0

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(getPreferenceXmlResId(), rootKey)

    prefThemePicker = findPreference(PREF_THEME_PICKER)
    prefColorPrimary = findPreference(PREF_COLOR_PRIMARY)
    prefColorAccent = findPreference(PREF_COLOR_ACCENT)
    prefColorBackground = findPreference(PREF_COLOR_BACKGROUND)
    prefColorNavBar = findPreference(PREF_COLOR_NAV_BAR)

    prefColorPrimary.saveValue(cyanea.primary)
    prefColorAccent.saveValue(cyanea.accent)
    prefColorBackground.saveValue(cyanea.backgroundColor)

    prefThemePicker.onPreferenceClickListener = this
    prefColorPrimary.onPreferenceChangeListener = this
    prefColorAccent.onPreferenceChangeListener = this
    prefColorBackground.onPreferenceChangeListener = this
    prefColorNavBar.onPreferenceChangeListener = this

    setupNavBarPref()
  }

  override fun onPreferenceClick(preference: Preference?): Boolean {
    return when (preference) {
      prefThemePicker -> {
        activity?.run {
          if (this is CyaneaThemePickerLauncher) {
            launchThemePicker()
          } else {
            startActivity(Intent(this, CyaneaThemePickerActivity::class.java))
          }
        }
        true
      }
      else -> false
    }
  }

  override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
    fun editTheme(action: (editor: Cyanea.Editor) -> Unit) {
      val activity = activity ?: return
      cyanea.edit { action(this) }
      Handler().postDelayed({
        activity.finish()
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(activity.intent)
      }, 200)
      //recreate(activity, smooth = true)
    }

    when (preference) {
      prefColorPrimary -> editTheme { it.primary(newValue as Int) }
      prefColorAccent -> editTheme { it.accent(newValue as Int) }
      prefColorBackground -> editTheme { it.background(newValue as Int) }
      prefColorNavBar -> editTheme { it.shouldTintNavBar(newValue as Boolean) }
      else -> return false
    }

    return true
  }

  override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
    return object : PreferenceGroupAdapter(preferenceScreen) {
      @SuppressLint("RestrictedApi")
      override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (iconSpaceReserved) return
        // See: https://stackoverflow.com/a/51568782/1048340
        val preference = getItem(position)
        if (preference is PreferenceCategory) {
          setZeroPaddingToLayoutChildren(holder.itemView)
        } else {
          holder.itemView.findViewById<View>(R.id.icon_frame)?.let { iconFrame ->
            iconFrame.visibility = if (preference.icon == null) View.GONE else View.VISIBLE
          }
        }
      }
    }
  }

  // See: https://stackoverflow.com/a/51568782/1048340
  private fun setZeroPaddingToLayoutChildren(view: View) {
    if (view !is ViewGroup) return
    for (i in 0 until view.childCount) {
      setZeroPaddingToLayoutChildren(view.getChildAt(i))
      view.setPaddingRelative(0, view.paddingTop, view.paddingEnd, view.paddingBottom)
    }
  }

  private fun setupNavBarPref() {
    prefColorNavBar.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ||
      ColorUtils.isDarkColor(cyanea.primary, 0.75)

    val isColored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      activity?.window?.navigationBarColor == cyanea.primary
    } else false
    prefColorNavBar.isChecked = cyanea.shouldTintNavBar || isColored
    val sysBarConfig = SystemBarTint(activity!!).sysBarConfig
    if (!sysBarConfig.hasNavigationBar) {
      findPreference<PreferenceCategory>(PREF_CATEGORY).run {
        removePreference(prefColorNavBar)
      }
    }
  }

  private inline fun <reified T : Preference> findPreference(key: String): T = super.findPreference(key) as T

  private companion object {
    const val PREF_CATEGORY = "cyanea_preference_category"
    const val PREF_THEME_PICKER = "pref_theme_picker"
    const val PREF_COLOR_PRIMARY = "pref_color_primary"
    const val PREF_COLOR_ACCENT = "pref_color_accent"
    const val PREF_COLOR_BACKGROUND = "pref_color_background"
    const val PREF_COLOR_NAV_BAR = "pref_color_navigation_bar"
  }

}
