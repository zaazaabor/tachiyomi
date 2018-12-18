/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import java.util.Locale

inline class Language(val code: String) {

  fun toEmoji(): String? {
    val locale = when (code) {
      "de" -> Locale("de", "DE")
      "fr" -> Locale("fr", "FR")
      "en" -> Locale("en", "GB")
      "es" -> Locale("es", "ES")
      "it" -> Locale("it", "IT")
      "ja" -> Locale("ja", "JP")
      "pt" -> Locale("pt", "BR")
      "ru" -> Locale("ru", "RU")
      "vi" -> Locale("vi", "VN")
      "zh" -> Locale("zh", "CN")
      else -> null
    }
    return locale?.toFlag()
  }

  fun Locale.toFlag(): String {
    return try {
      val countryCode = country
      val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
      val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
      String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    } catch (e: Exception) {
      ""
    }
  }

}
