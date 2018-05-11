package tachiyomi.source

import tachiyomi.core.http.Http
import tachiyomi.core.prefs.PreferenceStore

class Dependencies(
  val http: Http,
  val preferences: PreferenceStore
)
