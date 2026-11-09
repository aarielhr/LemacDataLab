package com.example.lemacdatalab

import com.example.lemacdatalab.ui.viewmodel.NotificationViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NotificationViewModelTest {

    private lateinit var viewModel: NotificationViewModel

    @Before
    fun setUp() {
        viewModel = NotificationViewModel()
    }

    @Test
    fun `estado inicial de notificaciones es deshabilitado con valores por defecto`() {
        assertFalse(viewModel.uiState.value.notificacionesHabilitadas)
        assertEquals(20, viewModel.uiState.value.hora)
        assertEquals(0, viewModel.uiState.value.minuto)
        assertNull(viewModel.uiState.value.mensajeConfirmacion)
    }

    @Test
    fun `limpiar mensaje reinicia mensajeConfirmacion a null`() {
        viewModel.limpiarMensaje()
        assertNull(viewModel.uiState.value.mensajeConfirmacion)
    }
}
