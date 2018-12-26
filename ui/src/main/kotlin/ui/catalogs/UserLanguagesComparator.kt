/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import androidx.core.os.LocaleListCompat

class UserLanguagesComparator : Comparator<Language> {

  private val userLanguages = mutableSetOf<String>()

  init {
    val userLocales = LocaleListCompat.getDefault()
    for (i in 0 until userLocales.size()) {
      userLanguages.add(userLocales[i].language)
    }
  }

  override fun compare(langOne: Language, langTwo: Language): Int {
    val langOnePosition = userLanguages.indexOf(langOne.code)
    val langTwoPosition = userLanguages.indexOf(langTwo.code)

    return when {
      langOnePosition != -1 && langTwoPosition != -1 -> langOnePosition.compareTo(langTwoPosition)
      langOnePosition != -1 -> -1
      langTwoPosition != -1 -> 1
      else -> langOne.code.compareTo(langTwo.code)
    }
  }

}
