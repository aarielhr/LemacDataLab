package com.example.lemacdatalab

import com.example.lemacdatalab.data.model.Ambitos
import com.example.lemacdatalab.data.repository.InMemoryRegistroRepository
import com.example.lemacdatalab.ui.viewmodel.RegistroViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegistroViewModelTest {

    private lateinit var repository: InMemoryRegistroRepository
    private lateinit var viewModel: RegistroViewModel

    @Before
    fun setUp() {
        repository = InMemoryRegistroRepository()
        viewModel = RegistroViewModel(repository)
    }

    @Test
    fun `tipo de actividad vacio muestra error y no guarda`() {
        val cantidadInicial = viewModel.registros.value.size

        val resultado = viewModel.validarYGuardar()

        assertFalse(resultado)
        assertNotNull(viewModel.formUiState.value.tipoActividadError)
        assertEquals(cantidadInicial, viewModel.registros.value.size)
    }

    @Test
    fun `DBT sin desencadenante muestra error de regla de dominio`() {
        viewModel.onAmbitoChanged(Ambitos.HABILIDADES_DBT)
        viewModel.onTipoActividadChanged("Mente Sabia")

        assertFalse(viewModel.validarYGuardar())
        assertNotNull(viewModel.formUiState.value.desencadenanteError)
    }

    @Test
    fun `Adicciones sin desencadenante muestra error de regla de dominio`() {
        viewModel.onAmbitoChanged(Ambitos.ADICCIONES)
        viewModel.onTipoActividadChanged("Control de Craving / Impulso")

        assertFalse(viewModel.validarYGuardar())
        assertNotNull(viewModel.formUiState.value.desencadenanteError)
    }

    @Test
    fun `Neurodesarrollo no exige desencadenante`() {
        viewModel.onAmbitoChanged(Ambitos.NEURODESARROLLO)
        viewModel.onTipoActividadChanged("Pausa de autorregulación")

        assertTrue(viewModel.validarYGuardar())
        assertNull(viewModel.formUiState.value.desencadenanteError)
    }

    @Test
    fun `datos validos agregan el registro al inicio y lo persisten`() {
        val cantidadInicial = viewModel.registros.value.size
        viewModel.onAmbitoChanged(Ambitos.HABILIDADES_DBT)
        viewModel.onTipoActividadChanged("Mente Sabia")
        viewModel.onDesencadenanteChanged("Discusión en el trabajo")
        viewModel.onNivelIntensidadChanged(9f)

        assertTrue(viewModel.validarYGuardar())

        val nuevoId = viewModel.formUiState.value.registroCreadoId
        assertNotNull(nuevoId)
        assertEquals(cantidadInicial + 1, viewModel.registros.value.size)
        assertEquals(nuevoId, viewModel.registros.value.first().id)
        assertEquals(9, viewModel.registros.value.first().nivelIntensidad)
        // Un ViewModel nuevo sobre el mismo repositorio ve el registro guardado
        assertNotNull(RegistroViewModel(repository).getRegistroById(nuevoId!!))
    }

    @Test
    fun `la intensidad se limita al rango 1 a 10`() {
        viewModel.onNivelIntensidadChanged(42f)
        assertEquals(10f, viewModel.formUiState.value.nivelIntensidad, 0f)

        viewModel.onNivelIntensidadChanged(-3f)
        assertEquals(1f, viewModel.formUiState.value.nivelIntensidad, 0f)
    }

    @Test
    fun `eliminar quita el registro`() {
        val id = viewModel.registros.value.first().id

        viewModel.eliminarRegistro(id)

        assertNull(viewModel.getRegistroById(id))
    }

    @Test
    fun `borrar todos los registros no hace reaparecer los de ejemplo`() {
        viewModel.registros.value.map { it.id }.forEach { viewModel.eliminarRegistro(it) }

        assertTrue(viewModel.registros.value.isEmpty())
        assertTrue(RegistroViewModel(repository).registros.value.isEmpty())
    }

    @Test
    fun `cambiar de ambito limpia el tipo de actividad`() {
        viewModel.onTipoActividadChanged("Algo")
        viewModel.onAmbitoChanged(Ambitos.ESTADO_ANIMO)

        assertEquals("", viewModel.formUiState.value.tipoActividad)
    }
}
