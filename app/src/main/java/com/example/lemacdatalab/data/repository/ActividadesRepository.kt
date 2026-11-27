package com.example.lemacdatalab.data.repository

import com.example.lemacdatalab.data.model.Actividad
import com.example.lemacdatalab.data.remote.ActividadesApi
import com.example.lemacdatalab.data.remote.ApiClient

interface ActividadesRepository {
    suspend fun cargarActividades(): List<Actividad>
}

class RetrofitActividadesRepository(
    private val api: ActividadesApi = ApiClient.actividadesApi
) : ActividadesRepository {
    override suspend fun cargarActividades(): List<Actividad> = api.getActividades()
}
