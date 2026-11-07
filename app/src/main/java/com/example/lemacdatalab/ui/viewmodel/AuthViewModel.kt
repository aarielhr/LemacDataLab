package com.example.lemacdatalab.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.lemacdatalab.data.repository.InMemoryUserRepository
import com.example.lemacdatalab.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel(
    private val userRepository: UserRepository = InMemoryUserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(sesionUsuario = userRepository.getSesionActual())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onAliasLoginChanged(value: String) {
        _uiState.update { it.copy(aliasLogin = value, errorAuth = null) }
    }

    fun onPasswordLoginChanged(value: String) {
        _uiState.update { it.copy(passwordLogin = value, errorAuth = null) }
    }

    fun onAliasRegistroChanged(value: String) {
        _uiState.update { it.copy(aliasRegistro = value, errorAuth = null) }
    }

    fun onEmailRegistroChanged(value: String) {
        _uiState.update { it.copy(emailRegistro = value, errorAuth = null) }
    }

    fun onPasswordRegistroChanged(value: String) {
        _uiState.update { it.copy(passwordRegistro = value, errorAuth = null) }
    }

    fun ejecutarLogin(): Boolean {
        val state = _uiState.value
        if (state.aliasLogin.isBlank() || state.passwordLogin.isBlank()) {
            _uiState.update { it.copy(errorAuth = "Ingresa tu usuario/correo y contraseña") }
            return false
        }

        val usuario = userRepository.validarLogin(state.aliasLogin, state.passwordLogin)
        if (usuario != null) {
            _uiState.update {
                it.copy(
                    sesionUsuario = usuario.alias,
                    aliasLogin = "",
                    passwordLogin = "",
                    errorAuth = null,
                    mensajeExito = "¡Bienvenido/a ${usuario.alias}!"
                )
            }
            return true
        } else {
            _uiState.update { it.copy(errorAuth = "Usuario o contraseña incorrectos") }
            return false
        }
    }

    fun ejecutarRegistro(): Boolean {
        val state = _uiState.value
        val (exito, mensaje) = userRepository.registrarUsuario(
            alias = state.aliasRegistro,
            email = state.emailRegistro,
            password = state.passwordRegistro
        )

        if (exito) {
            _uiState.update {
                it.copy(
                    sesionUsuario = state.aliasRegistro.trim(),
                    aliasRegistro = "",
                    emailRegistro = "",
                    passwordRegistro = "",
                    errorAuth = null,
                    mensajeExito = mensaje
                )
            }
            return true
        } else {
            _uiState.update { it.copy(errorAuth = mensaje) }
            return false
        }
    }

    fun cerrarSesion() {
        userRepository.guardarSesionActual(null)
        _uiState.update {
            it.copy(
                sesionUsuario = null,
                mensajeExito = "Sesión cerrada correctamente"
            )
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(errorAuth = null, mensajeExito = null) }
    }
}
