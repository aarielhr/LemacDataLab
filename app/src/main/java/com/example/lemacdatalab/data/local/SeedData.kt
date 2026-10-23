package com.example.lemacdatalab.data.local

import com.example.lemacdatalab.data.model.Ambitos
import com.example.lemacdatalab.data.model.RegistroAutoReporte
import com.example.lemacdatalab.data.model.Solicitud
import java.util.Calendar

/** Datos de ejemplo que se muestran solo la primera vez que se abre la app. */
object SeedData {

    fun fechaMillis(anio: Int, mes: Int, dia: Int, hora: Int = 0, minuto: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(anio, mes - 1, dia, hora, minuto, 0)
        }.timeInMillis

    val registrosIniciales = listOf(
        RegistroAutoReporte(
            id = "reg-1",
            aliasUsuario = "Usuario_Prueba_01",
            ambito = Ambitos.NEURODESARROLLO,
            tipoActividad = "Pausa de autorregulación",
            nivelIntensidad = 7,
            desencadenanteEstrategia = "Estrategia de respiración diafragmática de 5 minutos",
            observacionesContexto = "Lugar ruidoso en la tarde. Ayudó a reducir sobrecarga.",
            fechaHora = fechaMillis(2026, 9, 28, 10, 30)
        ),
        RegistroAutoReporte(
            id = "reg-2",
            aliasUsuario = "Usuario_Prueba_01",
            ambito = Ambitos.HABILIDADES_DBT,
            tipoActividad = "Aceptación Radical",
            nivelIntensidad = 8,
            desencadenanteEstrategia = "Frustración por retraso en proyecto personal",
            observacionesContexto = "Practicado ejercicio de mente sabia. Utilidad alta.",
            fechaHora = fechaMillis(2026, 9, 27, 18, 15)
        ),
        RegistroAutoReporte(
            id = "reg-3",
            aliasUsuario = "Usuario_Prueba_01",
            ambito = Ambitos.ESTADO_ANIMO,
            tipoActividad = "Check-in: Calma / Tranquilidad",
            nivelIntensidad = 6,
            desencadenanteEstrategia = "Caminata al aire libre",
            observacionesContexto = "Sensación de bienestar en nivel medio-alto.",
            fechaHora = fechaMillis(2026, 9, 27, 9, 0)
        ),
        RegistroAutoReporte(
            id = "reg-4",
            aliasUsuario = "Usuario_Prueba_01",
            ambito = Ambitos.ADICCIONES,
            tipoActividad = "Control de Craving / Impulso",
            nivelIntensidad = 6,
            desencadenanteEstrategia = "Estrés laboral ➔ Acción protectora: llamar a contacto de apoyo",
            observacionesContexto = "Se evitó el consumo impulsivo exitosamente.",
            fechaHora = fechaMillis(2026, 9, 26, 21, 45)
        )
    )

    val solicitudesIniciales = listOf(
        Solicitud(
            id = "sol-1",
            codigoMuestra = "MUESTRA-H-01",
            solicitante = "Constructora Andes Ltda.",
            tipoEnsayo = "Hormigón / Cemento",
            cantidadMuestras = 3,
            esUrgente = true,
            observaciones = "Ensayo de compresión a 28 días",
            fechaRegistro = fechaMillis(2026, 9, 28)
        ),
        Solicitud(
            id = "sol-2",
            codigoMuestra = "MUESTRA-S-02",
            solicitante = "Inmobiliaria Bío-Bío",
            tipoEnsayo = "Suelos / Geotecnia",
            cantidadMuestras = 5,
            esUrgente = false,
            observaciones = "Análisis granulométrico y proctor modificado",
            fechaRegistro = fechaMillis(2026, 9, 27)
        )
    )
}
