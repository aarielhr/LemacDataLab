package com.example.lemacdatalab

import com.example.lemacdatalab.data.model.Actividad
import com.example.lemacdatalab.data.repository.ActividadesRepository
import com.example.lemacdatalab.ui.viewmodel.ActividadesUiState
import com.example.lemacdatalab.ui.viewmodel.ActividadesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActividadesViewModelTest {

    @Test
    fun `cargar actividades expone datos recibidos`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val actividades = listOf(Actividad("Pausa de autorregulación"))
            val viewModel = ActividadesViewModel(object : ActividadesRepository {
                override suspend fun cargarActividades() = actividades
            })

            viewModel.cargarActividades()
            assertEquals(ActividadesUiState.Cargando, viewModel.uiState.value)
            advanceUntilIdle()

            assertEquals(ActividadesUiState.Datos(actividades), viewModel.uiState.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `cargar actividades expone error`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = ActividadesViewModel(object : ActividadesRepository {
                override suspend fun cargarActividades(): List<Actividad> {
                    error("Sin conexión")
                }
            })

            viewModel.cargarActividades()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is ActividadesUiState.Error)
            assertEquals("Sin conexión", (viewModel.uiState.value as ActividadesUiState.Error).mensaje)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
