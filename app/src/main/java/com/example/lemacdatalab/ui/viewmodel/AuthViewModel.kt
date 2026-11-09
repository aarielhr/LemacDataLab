package com.example.lemacdatalab.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.lemacdatalab.data.model.Usuario
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

    private var ultimoAccesoMillis: Long = System.currentTimeMillis()

    fun registrarActividadUsuario() {
        ultimoAccesoMillis = System.currentTimeMillis()
    }

    fun verificarInactividadExcedida(maxInactividadMillis: Long): Boolean {
        val state = _uiState.value
        if (state.sesionUsuario == null || state.sesionBloqueada) return false
        return System.currentTimeMillis() - ultimoAccesoMillis >= maxInactividadMillis
    }

    fun bloquearSesionPorInactividad() {
        if (_uiState.value.sesionUsuario != null && !_uiState.value.sesionBloqueada) {
            _uiState.update {
                it.copy(
                    sesionBloqueada = true,
                    razonBloqueo = "Sesión pausada tras 5 minutos de inactividad por seguridad.",
                    passwordReingreso = "",
                    errorAuth = null
                )
            }
        }
    }

    fun bloquearSesionPorSegundoPlano() {
        if (_uiState.value.sesionUsuario != null && !_uiState.value.sesionBloqueada) {
            _uiState.update {
                it.copy(
                    sesionBloqueada = true,
                    razonBloqueo = "Sesión pausada al pasar la aplicación a segundo plano.",
                    passwordReingreso = "",
                    errorAuth = null
                )
            }
        }
    }

    fun onPasswordReingresoChanged(value: String) {
        _uiState.update { it.copy(passwordReingreso = value, errorAuth = null) }
    }

    fun reingresarConPassword(): Boolean {
        val state = _uiState.value
        val usuarioAlias = state.sesionUsuario ?: return false
        if (state.passwordReingreso.isBlank()) {
            _uiState.update { it.copy(errorAuth = "Ingresa tu contraseña para desbloquear") }
            return false
        }

        val usuarioValidado = userRepository.validarLogin(usuarioAlias, state.passwordReingreso)
        if (usuarioValidado != null) {
            registrarActividadUsuario()
            _uiState.update {
                it.copy(
                    sesionBloqueada = false,
                    razonBloqueo = null,
                    passwordReingreso = "",
                    errorAuth = null,
                    mensajeExito = "¡Sesión reanudada!"
                )
            }
            return true
        } else {
            _uiState.update { it.copy(errorAuth = "Contraseña incorrecta para reingresar") }
            return false
        }
    }

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

    fun onConsentimientoChanged(aceptado: Boolean) {
        _uiState.update { it.copy(consentimientoAceptado = aceptado, errorAuth = null) }
    }

    fun onMostrarDialogoConsentimiento(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarDialogoConsentimiento = mostrar) }
    }

    fun getUsuarioActual(): Usuario? {
        val alias = _uiState.value.sesionUsuario
        return userRepository.getUsuarioActual(alias)
    }

    fun ejecutarLogin(): Boolean {
        val state = _uiState.value
        if (state.aliasLogin.isBlank() || state.passwordLogin.isBlank()) {
            _uiState.update { it.copy(errorAuth = "Ingresa tu usuario/correo y contraseña") }
            return false
        }

        val usuario = userRepository.validarLogin(state.aliasLogin, state.passwordLogin)
        if (usuario != null) {
            registrarActividadUsuario()
            _uiState.update {
                it.copy(
                    sesionUsuario = usuario.alias,
                    sesionBloqueada = false,
                    razonBloqueo = null,
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
        if (!state.consentimientoAceptado) {
            _uiState.update { it.copy(errorAuth = "Debes aceptar el consentimiento informado para poder registrarte.") }
            return false
        }

        val (exito, mensaje) = userRepository.registrarUsuario(
            alias = state.aliasRegistro,
            email = state.emailRegistro,
            password = state.passwordRegistro,
            consentimientoAceptado = state.consentimientoAceptado
        )

        if (exito) {
            registrarActividadUsuario()
            _uiState.update {
                it.copy(
                    sesionUsuario = state.aliasRegistro.trim(),
                    sesionBloqueada = false,
                    razonBloqueo = null,
                    aliasRegistro = "",
                    emailRegistro = "",
                    passwordRegistro = "",
                    consentimientoAceptado = false,
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
                sesionBloqueada = false,
                razonBloqueo = null,
                passwordReingreso = "",
                mensajeExito = "Sesión cerrada correctamente"
            )
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(errorAuth = null, mensajeExito = null) }
    }
}
