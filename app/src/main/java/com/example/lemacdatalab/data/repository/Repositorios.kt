package com.example.lemacdatalab.data.repository

import android.content.Context
import com.example.lemacdatalab.data.local.LocalPreferencesStorage
import com.example.lemacdatalab.data.local.SeedData
import com.example.lemacdatalab.data.model.RegistroAutoReporte


interface RegistroRepository {
    fun cargar(): List<RegistroAutoReporte>
    fun guardar(lista: List<RegistroAutoReporte>)
}


/** Implementación real: persiste en SharedPreferences. */
class PreferencesRegistroRepository(context: Context) : RegistroRepository {
    private val appContext = context.applicationContext
    override fun cargar() = LocalPreferencesStorage.cargarRegistros(appContext)
    override fun guardar(lista: List<RegistroAutoReporte>) =
        LocalPreferencesStorage.guardarRegistros(appContext, lista)
}

/** Implementación en memoria: para tests y previews (no necesita Android). */
class InMemoryRegistroRepository(
    private var datos: List<RegistroAutoReporte> = SeedData.registrosIniciales
) : RegistroRepository {
    override fun cargar() = datos
    override fun guardar(lista: List<RegistroAutoReporte>) { datos = lista }
}

