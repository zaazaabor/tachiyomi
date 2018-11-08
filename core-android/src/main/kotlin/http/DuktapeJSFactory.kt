package tachiyomi.core.http

import com.squareup.duktape.Duktape
import javax.inject.Inject

/**
 * A factory for creating instances of [DuktapeJS].
 */
internal class DuktapeJSFactory @Inject constructor() : JSFactory {

  /**
   * Returns a new instance of [DuktapeJS].
   */
  override fun create(): JS {
    return DuktapeJS(Duktape.create())
  }

}
