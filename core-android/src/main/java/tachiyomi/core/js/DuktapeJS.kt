package tachiyomi.core.js

import com.squareup.duktape.Duktape

internal class DuktapeJS(private val duktape: Duktape) : JS {

  override fun evaluate(script: String): Any {
    return duktape.evaluate(script)
  }

  override fun close() {
    duktape.close()
  }

}
