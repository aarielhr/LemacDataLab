package com.example.lemacdatalab.data.remote

import com.example.lemacdatalab.data.model.Actividad
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ActividadesApi {
    @GET("actividades")
    suspend fun getActividades(): List<Actividad>
}

object ApiClient {
    val actividadesApi: ActividadesApi by lazy {
        Retrofit.Builder()
            .baseUrl(com.example.lemacdatalab.BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ActividadesApi::class.java)
    }
}
