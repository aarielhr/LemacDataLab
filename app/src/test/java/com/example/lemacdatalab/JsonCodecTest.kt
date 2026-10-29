package com.example.lemacdatalab

import com.example.lemacdatalab.data.local.JsonCodec
import com.example.lemacdatalab.data.local.SeedData
import com.example.lemacdatalab.data.model.RegistroAutoReporte
import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonCodecTest {

    @Test
    fun `registros hacen ida y vuelta sin perder datos`() {
        val original = SeedData.registrosIniciales

        val recuperado = JsonCodec.registrosDesdeJson(JsonCodec.registrosAJson(original))

        assertEquals(original, recuperado)
    }

    @Test
    fun `registros con latitud y longitud conservan datos de ubicacion GPS`() {
        val registroConGps = RegistroAutoReporte(
            id = "gps-1",
            aliasUsuario = "Usuario_GPS",
            ambito = "Neurodesarrollo",
            tipoActividad = "Pausa de autorregulación",
            nivelIntensidad = 8,
            desencadenanteEstrategia = "Estrategia",
            observacionesContexto = "En el parque",
            fechaHora = 1700000000000L,
            latitud = -34.9011,
            longitud = -56.1645
        )

        val json = JsonCodec.registrosAJson(listOf(registroConGps))
        val recuperado = JsonCodec.registrosDesdeJson(json).single()

        assertEquals(-34.9011, recuperado.latitud!!, 0.0001)
        assertEquals(-56.1645, recuperado.longitud!!, 0.0001)
    }

    @Test
    fun `una lista vacia sigue vacia (no se reemplaza por los datos de ejemplo)`() {
        val json = JsonCodec.registrosAJson(emptyList())

        assertEquals("[]", json)
        assertTrue(JsonCodec.registrosDesdeJson(json).isEmpty())
    }

    @Test
    fun `fecha en formato antiguo de texto se migra a epoch`() {
        val json = """[{"id":"a","aliasUsuario":"u","ambito":"x","tipoActividad":"t",
            "nivelIntensidad":4,"desencadenanteEstrategia":"","observacionesContexto":"",
            "fechaHora":"28/09/2026 10:30"}]"""

        val registro = JsonCodec.registrosDesdeJson(json).single()

        assertEquals(SeedData.fechaMillis(2026, 9, 28, 10, 30), registro.fechaHora)
        assertNull(registro.latitud)
        assertNull(registro.longitud)
    }

    @Test(expected = JSONException::class)
    fun `json corrupto lanza JSONException`() {
        JsonCodec.registrosDesdeJson("esto no es json")
    }
}
