package com.example.lemacdatalab.ui.viewmodel

data class AuthUiState(
    val aliasLogin: String = "",
    val passwordLogin: String = "",

    val aliasRegistro: String = "",
    val emailRegistro: String = "",
    val passwordRegistro: String = "",
    val consentimientoAceptado: Boolean = false,
    val mostrarDialogoConsentimiento: Boolean = false,

    // Bloqueo de sesión por inactividad o segundo plano
    val sesionBloqueada: Boolean = false,
    val razonBloqueo: String? = null,
    val passwordReingreso: String = "",

    val errorAuth: String? = null,
    val mensajeExito: String? = null,
    val sesionUsuario: String? = null
)
