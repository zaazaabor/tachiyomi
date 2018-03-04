package tachiyomi.core.http

import io.reactivex.Single
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

fun Call.asSingle(): Single<Response> {
  return Single.fromCallable {
    clone().execute()
  }
}

fun Call.asSingleSuccess(): Single<Response> {
  return asSingle().doOnSuccess { response ->
    if (!response.isSuccessful) {
      response.close()
      throw Exception("HTTP error ${response.code()}")
    }
  }
}

fun OkHttpClient.newCallWithProgress(request: Request, listener: ProgressListener): Call {
  val progressClient = newBuilder()
    .cache(null)
    .addNetworkInterceptor { chain ->
      val originalResponse = chain.proceed(chain.request())
      originalResponse.newBuilder()
        .body(ProgressResponseBody(originalResponse.body()!!, listener))
        .build()
    }
    .build()

  return progressClient.newCall(request)
}
