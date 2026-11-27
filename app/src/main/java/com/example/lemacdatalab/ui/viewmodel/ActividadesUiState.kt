package com.example.lemacdatalab.ui.viewmodel

import com.example.lemacdatalab.data.model.Actividad

sealed interface ActividadesUiState {
    data object Cargando : ActividadesUiState
    data class Datos(val actividades: List<Actividad>) : ActividadesUiState
    data class Error(val mensaje: String) : ActividadesUiState
}
