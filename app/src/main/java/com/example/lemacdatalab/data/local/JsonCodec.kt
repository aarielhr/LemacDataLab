package com.example.lemacdatalab.data.local

import com.example.lemacdatalab.data.model.RegistroAutoReporte
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Conversión de modelos a/desde JSON. Son funciones puras (sin Context),
 * por eso se pueden probar con tests unitarios.
 * Lanza org.json.JSONException si el texto no es un JSON válido.
 */
object JsonCodec {

    fun registrosAJson(lista: List<RegistroAutoReporte>): String {
        val array = JSONArray()
        for (item in lista) {
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("aliasUsuario", item.aliasUsuario)
                    put("ambito", item.ambito)
                    put("tipoActividad", item.tipoActividad)
                    put("nivelIntensidad", item.nivelIntensidad)
                    put("desencadenanteEstrategia", item.desencadenanteEstrategia)
                    put("observacionesContexto", item.observacionesContexto)
                    put("fechaHora", item.fechaHora)
                }
            )
        }
        return array.toString()
    }

    fun registrosDesdeJson(json: String): List<RegistroAutoReporte> {
        val array = JSONArray(json)
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            RegistroAutoReporte(
                id = obj.optString("id", ""),
                aliasUsuario = obj.optString("aliasUsuario", ""),
                ambito = obj.optString("ambito", ""),
                tipoActividad = obj.optString("tipoActividad", ""),
                nivelIntensidad = obj.optInt("nivelIntensidad", 5),
                desencadenanteEstrategia = obj.optString("desencadenanteEstrategia", ""),
                observacionesContexto = obj.optString("observacionesContexto", ""),
                fechaHora = leerFecha(obj, "fechaHora")
            )
        }
    }


    /**
     * Lee la fecha como número. Si viene como texto (formato antiguo "dd/MM/yyyy HH:mm"),
     * la convierte para no perder los datos de quien ya tenía la app instalada.
     */
    private fun leerFecha(obj: JSONObject, clave: String): Long =
        when (val valor = obj.opt(clave)) {
            is Number -> valor.toLong()
            is String -> parsearFechaAntigua(valor) ?: System.currentTimeMillis()
            else -> System.currentTimeMillis()
        }

    private fun parsearFechaAntigua(texto: String): Long? {
        for (patron in listOf("dd/MM/yyyy HH:mm", "dd/MM/yyyy")) {
            try {
                val fecha = SimpleDateFormat(patron, Locale.getDefault()).parse(texto)
                if (fecha != null) return fecha.time
            } catch (e: ParseException) {
                // probar el siguiente patrón
            }
        }
        return null
    }
}
