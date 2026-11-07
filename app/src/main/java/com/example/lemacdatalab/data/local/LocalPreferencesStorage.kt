package com.example.lemacdatalab.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.lemacdatalab.data.model.RegistroAutoReporte
import com.example.lemacdatalab.data.model.Usuario
import org.json.JSONException

object LocalPreferencesStorage {

    private const val PREFS_NAME = "lemac_datalab_prefs"
    private const val KEY_REGISTROS = "key_registros"
    private const val KEY_USUARIOS = "key_usuarios"
    private const val KEY_SESION_USUARIO = "key_sesion_usuario"
    private const val SUFIJO_CORRUPTO = "_corrupto"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun guardarRegistros(context: Context, lista: List<RegistroAutoReporte>) {
        prefs(context).edit().putString(KEY_REGISTROS, JsonCodec.registrosAJson(lista)).apply()
    }

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

    fun guardarUsuarios(context: Context, lista: List<Usuario>) {
        prefs(context).edit().putString(KEY_USUARIOS, JsonCodec.usuariosAJson(lista)).apply()
    }

    fun cargarUsuarios(context: Context): List<Usuario> {
        val p = prefs(context)
        val json = p.getString(KEY_USUARIOS, null)
        if (json.isNullOrBlank()) {
            val iniciales = listOf(Usuario("Usuario_Prueba_01", "prueba@lemac.cl", "1234"))
            guardarUsuarios(context, iniciales)
            return iniciales
        }
        return try {
            JsonCodec.usuariosDesdeJson(json)
        } catch (e: JSONException) {
            emptyList()
        }
    }

    fun guardarSesionUsuario(context: Context, alias: String?) {
        if (alias == null) {
            prefs(context).edit().remove(KEY_SESION_USUARIO).apply()
        } else {
            prefs(context).edit().putString(KEY_SESION_USUARIO, alias).apply()
        }
    }

    fun cargarSesionUsuario(context: Context): String? {
        return prefs(context).getString(KEY_SESION_USUARIO, null)
    }
}
