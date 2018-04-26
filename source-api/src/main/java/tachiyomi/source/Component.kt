package tachiyomi.source

import tachiyomi.core.http.Http
import tachiyomi.core.prefs.PreferenceStore

class Component(
  val http: Http,
  val preferences: Lazy<PreferenceStore>
)
