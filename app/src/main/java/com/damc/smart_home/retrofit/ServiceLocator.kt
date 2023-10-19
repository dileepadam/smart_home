package com.damc.smart_home.retrofit

class ServiceLocator {
    private var apiEndPoints: ApiEndPoint? = null

    val retrofitBuilder: RetrofitBuilder? = RetrofitBuilder()

    private var instance: ServiceLocator? = null

    constructor() {
        apiEndPoints = retrofitBuilder?.getRetrofitInstance()?.create(ApiEndPoint::class.java)
    }

    fun getInstance(): ServiceLocator? {
        if (instance == null) {
            instance = ServiceLocator()
        }
        return instance
    }

    fun getApi(): ApiEndPoint? {
        return apiEndPoints
    }
}