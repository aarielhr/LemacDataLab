package com.example.lemacdatalab.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.lemacdatalab.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun cargarConfiguracion(context: Context) {
        val habilitadas = NotificationHelper.estanHabilitadas(context)
        val hora = NotificationHelper.getHora(context)
        val minuto = NotificationHelper.getMinuto(context)
        _uiState.update {
            it.copy(
                notificacionesHabilitadas = habilitadas,
                hora = hora,
                minuto = minuto
            )
        }
    }

    fun onHabilitadasChanged(context: Context, habilitadas: Boolean) {
        val hora = _uiState.value.hora
        val minuto = _uiState.value.minuto
        NotificationHelper.guardarConfiguracion(context, habilitadas, hora, minuto)
        _uiState.update {
            it.copy(
                notificacionesHabilitadas = habilitadas,
                mensajeConfirmacion = if (habilitadas)
                    "Recordatorio programado diariamente a las %02d:%02d".format(hora, minuto)
                else
                    "Recordatorio diario desactivado"
            )
        }
    }

    fun onHoraMinutoChanged(context: Context, hora: Int, minuto: Int) {
        val habilitadas = _uiState.value.notificacionesHabilitadas
        NotificationHelper.guardarConfiguracion(context, habilitadas, hora, minuto)
        _uiState.update {
            it.copy(
                hora = hora,
                minuto = minuto,
                mensajeConfirmacion = if (habilitadas)
                    "Recordatorio programado diariamente a las %02d:%02d".format(hora, minuto)
                else null
            )
        }
    }

    fun enviarNotificacionPrueba(context: Context) {
        NotificationHelper.enviarNotificacionGenerica(context)
        _uiState.update {
            it.copy(mensajeConfirmacion = "Notificación de prueba enviada")
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeConfirmacion = null) }
    }
}
