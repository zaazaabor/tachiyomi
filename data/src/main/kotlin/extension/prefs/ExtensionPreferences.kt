/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.extension.prefs

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore

internal class ExtensionPreferences constructor(private val preferenceStore: PreferenceStore) {

  fun trustedSignatures(): Preference<Set<String>> {
    return preferenceStore.getStringSet("trusted_signatures", emptySet())
  }

}
