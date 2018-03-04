package tachiyomi.core.js

import java.io.Closeable

interface JS : Closeable {

  fun evaluate(script: String): Any
}
