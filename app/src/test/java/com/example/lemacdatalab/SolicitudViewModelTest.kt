package com.example.lemacdatalab

import com.example.lemacdatalab.data.repository.InMemorySolicitudRepository
import com.example.lemacdatalab.ui.viewmodel.SolicitudViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SolicitudViewModelTest {

    private lateinit var repository: InMemorySolicitudRepository
    private lateinit var viewModel: SolicitudViewModel

    @Before
    fun setUp() {
        repository = InMemorySolicitudRepository()
        viewModel = SolicitudViewModel(repository)
    }

    private fun completarFormularioValido() {
        viewModel.onCodigoChanged("MUESTRA-X-99")
        viewModel.onSolicitanteChanged("Laboratorio Test")
        viewModel.onTipoEnsayoChanged("Acero / Estructuras")
        viewModel.onCantidadChanged("2")
    }

    @Test
    fun `formulario vacio muestra errores en los campos obligatorios`() {
        assertFalse(viewModel.validarYGuardar())

        val estado = viewModel.formUiState.value
        assertNotNull(estado.codigoError)
        assertNotNull(estado.solicitanteError)
        assertNotNull(estado.tipoEnsayoError)
        assertNull(estado.cantidadError) // la cantidad por defecto es "1"
    }

    @Test
    fun `cantidad cero, negativa o no numerica es invalida`() {
        completarFormularioValido()
        for (invalida in listOf("0", "-4", "abc", "")) {
            viewModel.onCantidadChanged(invalida)
            assertFalse("Debería rechazar '$invalida'", viewModel.validarYGuardar())
            assertNotNull(viewModel.formUiState.value.cantidadError)
        }
    }

    @Test
    fun `datos validos agregan la solicitud y la persisten`() {
        val cantidadInicial = viewModel.solicitudes.value.size
        completarFormularioValido()
        viewModel.onUrgenteChanged(true)

        assertTrue(viewModel.validarYGuardar())

        val id = viewModel.formUiState.value.solicitudCreadaId
        assertNotNull(id)
        assertEquals(cantidadInicial + 1, viewModel.solicitudes.value.size)
        val guardada = viewModel.getSolicitudById(id!!)
        assertEquals(2, guardada?.cantidadMuestras)
        assertEquals(true, guardada?.esUrgente)
        assertNotNull(SolicitudViewModel(repository).getSolicitudById(id))
    }

    @Test
    fun `los textos se guardan sin espacios sobrantes`() {
        completarFormularioValido()
        viewModel.onCodigoChanged("  MUESTRA-X-99  ")
        viewModel.onCantidadChanged(" 3 ")

        assertTrue(viewModel.validarYGuardar())

        val guardada = viewModel.solicitudes.value.first()
        assertEquals("MUESTRA-X-99", guardada.codigoMuestra)
        assertEquals(3, guardada.cantidadMuestras)
    }

    @Test
    fun `limpiarMensajeExito reinicia mensaje e id`() {
        completarFormularioValido()
        viewModel.validarYGuardar()

        viewModel.limpiarMensajeExito()

        assertNull(viewModel.formUiState.value.mensajeExito)
        assertNull(viewModel.formUiState.value.solicitudCreadaId)
    }
}
