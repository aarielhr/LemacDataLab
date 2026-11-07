package com.example.lemacdatalab.ui.viewmodel

data class AuthUiState(
    val aliasLogin: String = "",
    val passwordLogin: String = "",

    val aliasRegistro: String = "",
    val emailRegistro: String = "",
    val passwordRegistro: String = "",

    val errorAuth: String? = null,
    val mensajeExito: String? = null,
    val sesionUsuario: String? = null
)
