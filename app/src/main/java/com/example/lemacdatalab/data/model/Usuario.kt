package com.example.lemacdatalab.data.model

data class Usuario(
    val alias: String,
    val email: String,
    val passwordHash: String,
    val consentimientoAceptado: Boolean = true,
    val fechaConsentimiento: Long = System.currentTimeMillis(),
    val versionConsentimiento: String = "v1.0 - 2026"
)
