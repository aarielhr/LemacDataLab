package com.example.lemacdatalab

import com.example.lemacdatalab.data.repository.InMemoryUserRepository
import com.example.lemacdatalab.ui.viewmodel.AuthViewModel
import com.example.lemacdatalab.utils.ConsentimientoInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthViewModelTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        userRepository = InMemoryUserRepository()
        viewModel = AuthViewModel(userRepository)
    }

    @Test
    fun `login con credenciales validas tiene exito`() {
        viewModel.onAliasLoginChanged("prueba@lemac.cl")
        viewModel.onPasswordLoginChanged("1234")

        assertTrue(viewModel.ejecutarLogin())
        assertEquals("Usuario_Prueba_01", viewModel.uiState.value.sesionUsuario)
        assertNull(viewModel.uiState.value.errorAuth)
    }

    @Test
    fun `login con credenciales incorrectas muestra error`() {
        viewModel.cerrarSesion()
        viewModel.onAliasLoginChanged("usuario_fake")
        viewModel.onPasswordLoginChanged("bad_pass")

        assertFalse(viewModel.ejecutarLogin())
        assertNull(viewModel.uiState.value.sesionUsuario)
        assertNotNull(viewModel.uiState.value.errorAuth)
    }

    @Test
    fun `registro sin consentimiento aceptado falla`() {
        viewModel.onAliasRegistroChanged("NuevoUsuario")
        viewModel.onEmailRegistroChanged("nuevo@lemac.cl")
        viewModel.onPasswordRegistroChanged("clave123")
        viewModel.onConsentimientoChanged(false)

        assertFalse(viewModel.ejecutarRegistro())
        assertNotNull(viewModel.uiState.value.errorAuth)
    }

    @Test
    fun `registro con datos validos y consentimiento guarda fecha y version`() {
        viewModel.onAliasRegistroChanged("NuevoUsuario")
        viewModel.onEmailRegistroChanged("nuevo@lemac.cl")
        viewModel.onPasswordRegistroChanged("clave123")
        viewModel.onConsentimientoChanged(true)

        assertTrue(viewModel.ejecutarRegistro())
        assertEquals("NuevoUsuario", viewModel.uiState.value.sesionUsuario)
        assertNull(viewModel.uiState.value.errorAuth)

        val usuarioCreado = viewModel.getUsuarioActual()
        assertNotNull(usuarioCreado)
        assertTrue(usuarioCreado!!.consentimientoAceptado)
        assertEquals(ConsentimientoInfo.VERSION, usuarioCreado.versionConsentimiento)
        assertTrue(usuarioCreado.fechaConsentimiento > 0)
    }

    @Test
    fun `registro con datos invalidos muestra error`() {
        viewModel.onAliasRegistroChanged("ab") // Muy corto
        viewModel.onEmailRegistroChanged("correo_invalido")
        viewModel.onPasswordRegistroChanged("123")
        viewModel.onConsentimientoChanged(true)

        assertFalse(viewModel.ejecutarRegistro())
        assertNotNull(viewModel.uiState.value.errorAuth)
    }

    @Test
    fun `bloqueo por inactividad activa estado sesionBloqueada`() {
        viewModel.bloquearSesionPorInactividad()

        assertTrue(viewModel.uiState.value.sesionBloqueada)
        assertNotNull(viewModel.uiState.value.razonBloqueo)
    }

    @Test
    fun `bloqueo por segundo plano activa estado sesionBloqueada`() {
        viewModel.bloquearSesionPorSegundoPlano()

        assertTrue(viewModel.uiState.value.sesionBloqueada)
        assertNotNull(viewModel.uiState.value.razonBloqueo)
    }

    @Test
    fun `reingreso con contraseña correcta desbloquea la sesion`() {
        viewModel.bloquearSesionPorSegundoPlano()
        viewModel.onPasswordReingresoChanged("1234")

        assertTrue(viewModel.reingresarConPassword())
        assertFalse(viewModel.uiState.value.sesionBloqueada)
        assertNull(viewModel.uiState.value.razonBloqueo)
    }

    @Test
    fun `reingreso con contraseña incorrecta muestra error`() {
        viewModel.bloquearSesionPorInactividad()
        viewModel.onPasswordReingresoChanged("clave_erronea")

        assertFalse(viewModel.reingresarConPassword())
        assertTrue(viewModel.uiState.value.sesionBloqueada)
        assertNotNull(viewModel.uiState.value.errorAuth)
    }

    @Test
    fun `cerrar sesion limpia la sesion activa`() {
        viewModel.cerrarSesion()

        assertNull(viewModel.uiState.value.sesionUsuario)
        assertNull(userRepository.getSesionActual())
    }
}
