package com.damc.smart_home.retrofit

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface ApiEndPoint {
    @GET("led2on")
    fun ligtOn(): Observable<Response<ResponseBody>>

    @GET("led2off")
    fun ligtOff(): Observable<Response<ResponseBody>>
}