package tachiyomi.core.js

import com.squareup.duktape.Duktape
import javax.inject.Inject

internal class DuktapeJSFactory @Inject constructor() : JSFactory {

  override fun create(): JS {
    return DuktapeJS(Duktape.create())
  }
}
