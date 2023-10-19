package com.damc.smart_home.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

class RetrofitBuilder {
    val BASE_URL: String = "http://192.168.131.75/"

    private var instance: Retrofit? = null

    fun getRetrofitInstance(): Retrofit? {
        if (instance == null) {
            try {
                val client: OkHttpClient = OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build()

                instance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        return instance
    }

}