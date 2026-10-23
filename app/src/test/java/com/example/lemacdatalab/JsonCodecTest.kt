package com.example.lemacdatalab

import com.example.lemacdatalab.data.local.JsonCodec
import com.example.lemacdatalab.data.local.SeedData
import org.json.JSONException
import org.junit.Assert.assertEquals
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
    fun `solicitudes hacen ida y vuelta sin perder datos`() {
        val original = SeedData.solicitudesIniciales

        val recuperado = JsonCodec.solicitudesDesdeJson(JsonCodec.solicitudesAJson(original))

        assertEquals(original, recuperado)
    }

    @Test
    fun `una lista vacia sigue vacia (no se reemplaza por los datos de ejemplo)`() {
        val json = JsonCodec.registrosAJson(emptyList())

        assertEquals("[]", json)
        assertTrue(JsonCodec.registrosDesdeJson(json).isEmpty())
        assertTrue(JsonCodec.solicitudesDesdeJson(JsonCodec.solicitudesAJson(emptyList())).isEmpty())
    }

    @Test
    fun `fecha en formato antiguo de texto se migra a epoch`() {
        val json = """[{"id":"a","aliasUsuario":"u","ambito":"x","tipoActividad":"t",
            "nivelIntensidad":4,"desencadenanteEstrategia":"","observacionesContexto":"",
            "fechaHora":"28/09/2026 10:30"}]"""

        val registro = JsonCodec.registrosDesdeJson(json).single()

        assertEquals(SeedData.fechaMillis(2026, 9, 28, 10, 30), registro.fechaHora)
    }

    @Test(expected = JSONException::class)
    fun `json corrupto lanza JSONException`() {
        JsonCodec.registrosDesdeJson("esto no es json")
    }
}
