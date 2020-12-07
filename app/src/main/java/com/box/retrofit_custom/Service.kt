package com.box.retrofit_custom

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
data class Git(
    val name:String,
    val full_name:String,
    val owner: Owner
)
data class Owner(
    val login:String? = null,
    val url:String? = null
)
interface Service{
    @GET("users/{user}/repos")
    suspend fun getUser(@Path("user") user:String): NetworkResponse<List<Git>, Error>

    @GET("users/{user}/repos")
    fun getUser1(@Path("user") user:String): Call<List<Git>>


    companion object{
        val service = Retrofit.Builder().baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .build()
    }
}
