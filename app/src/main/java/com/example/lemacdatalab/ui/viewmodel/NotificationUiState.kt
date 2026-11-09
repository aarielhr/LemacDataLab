package com.example.lemacdatalab.ui.viewmodel

data class NotificationUiState(
    val notificacionesHabilitadas: Boolean = false,
    val hora: Int = 20,
    val minuto: Int = 0,
    val mensajeConfirmacion: String? = null
)
