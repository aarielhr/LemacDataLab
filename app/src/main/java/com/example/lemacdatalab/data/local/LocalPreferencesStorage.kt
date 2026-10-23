package com.example.lemacdatalab.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.lemacdatalab.data.model.RegistroAutoReporte
import com.example.lemacdatalab.data.model.Solicitud
import org.json.JSONException

object LocalPreferencesStorage {

    private const val PREFS_NAME = "lemac_datalab_prefs"
    private const val KEY_REGISTROS = "key_registros"
    private const val KEY_SOLICITUDES = "key_solicitudes"
    private const val SUFIJO_CORRUPTO = "_corrupto"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun guardarRegistros(context: Context, lista: List<RegistroAutoReporte>) {
        prefs(context).edit().putString(KEY_REGISTROS, JsonCodec.registrosAJson(lista)).apply()
    }

    /**
     * - Sin clave guardada (primera ejecución): datos de ejemplo.
     * - Lista vacía guardada: lista vacía (el usuario borró todo, no se "resucitan" los ejemplos).
     * - JSON corrupto: se conserva una copia aparte y se devuelve lista vacía.
     */
    fun cargarRegistros(context: Context): List<RegistroAutoReporte> {
        val p = prefs(context)
        val json = p.getString(KEY_REGISTROS, null) ?: return SeedData.registrosIniciales
        return try {
            JsonCodec.registrosDesdeJson(json)
        } catch (e: JSONException) {
            p.edit().putString(KEY_REGISTROS + SUFIJO_CORRUPTO, json).apply()
            emptyList()
        }
    }

    fun guardarSolicitudes(context: Context, lista: List<Solicitud>) {
        prefs(context).edit().putString(KEY_SOLICITUDES, JsonCodec.solicitudesAJson(lista)).apply()
    }

    fun cargarSolicitudes(context: Context): List<Solicitud> {
        val p = prefs(context)
        val json = p.getString(KEY_SOLICITUDES, null) ?: return SeedData.solicitudesIniciales
        return try {
            JsonCodec.solicitudesDesdeJson(json)
        } catch (e: JSONException) {
            p.edit().putString(KEY_SOLICITUDES + SUFIJO_CORRUPTO, json).apply()
            emptyList()
        }
    }
}
