package com.box.retrofit_custom

import android.util.Log
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException
import java.lang.Exception
/**
 * ref: https://proandroiddev.com/create-retrofit-calladapter-for-coroutines-to-handle-response-as-states-c102440de37a
 *
 * @Success which is a data class that should contain the body of the success state of the request.
 * @ApiError which represents the non-2xx responses, it also contains the error body and the response status code.
 * @NetworkError which represents network failure such as no internet connection cases.
 * @UnknownError which represents unexpected exceptions occurred creating the request or processing the response, for example parsing issues.
 * */
internal class NetworkResponseCall<S:Any,E:Any>(
    private val delegate: Call<S>,
    private val errorConverter: Converter<ResponseBody,E>
) :Call<NetworkResponse<S, E>>{
    override fun clone(): Call<NetworkResponse<S, E>> = NetworkResponseCall(delegate.clone(), errorConverter)

    override fun execute(): Response<NetworkResponse<S, E>> = throw UnsupportedOperationException("NetworkResponseCall doesn't support execute")

    override fun enqueue(callback: Callback<NetworkResponse<S, E>>) {
        return delegate.enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val body = response.body()
                val code = response.code()
                val error = response.errorBody()
                Log.d("NetworkResponseCall", ">>  onResponse:  ####  $response  ####")
                if (response.isSuccessful) {
                    if (body != null) {
                        callback.onResponse(
                            this@NetworkResponseCall, Response.success(
                                NetworkResponse.Success(body)
                            )
                        )
                    } else {
                        // response is successful but the body is null
                        callback.onResponse(
                            this@NetworkResponseCall, Response.success(
                                NetworkResponse.UnknownError(null)
                            )
                        )
                    }
                } else {
                    val errorBody = when {
                        error == null -> null
                        error.contentLength() == 0L -> null
                        else -> try {
                            errorConverter.convert(error)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (errorBody != null) {
                        callback.onResponse(
                            this@NetworkResponseCall, Response.success(
                                NetworkResponse.ApiError(errorBody, code)
                            )
                        )
                    } else {
                        callback.onResponse(
                            this@NetworkResponseCall, Response.success(
                                NetworkResponse.UnknownError(null)
                            )
                        )
                    }
                }
            }

            override fun onFailure(call: Call<S>, t: Throwable) {
                val networkResponse = when (t) {
                    is IOException -> NetworkResponse.NetworkError(t)
                    else -> NetworkResponse.UnknownError(t)
                }
                callback.onResponse(this@NetworkResponseCall, Response.success(networkResponse))
            }

        })
    }

    override fun isExecuted(): Boolean =delegate.isExecuted

    override fun cancel(): Unit =delegate.cancel()

    override fun isCanceled(): Boolean =delegate.isCanceled

    override fun request(): Request =delegate.request()

    override fun timeout(): Timeout =delegate.timeout()

}