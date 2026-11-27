package com.example.lemacdatalab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lemacdatalab.data.repository.ActividadesRepository
import com.example.lemacdatalab.data.repository.RetrofitActividadesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActividadesViewModel(
    private val repository: ActividadesRepository = RetrofitActividadesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActividadesUiState>(ActividadesUiState.Cargando)
    val uiState: StateFlow<ActividadesUiState> = _uiState.asStateFlow()

    fun cargarActividades() {
        viewModelScope.launch {
            _uiState.value = ActividadesUiState.Cargando
            try {
                _uiState.value = ActividadesUiState.Datos(repository.cargarActividades())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = ActividadesUiState.Error(
                    error.message ?: "No se pudieron cargar las actividades"
                )
            }
        }
    }
}
