package com.example.lemacdatalab.data.repository

import android.content.Context
import com.example.lemacdatalab.data.local.LocalPreferencesStorage
import com.example.lemacdatalab.data.model.Usuario
import com.example.lemacdatalab.utils.ConsentimientoInfo
import com.example.lemacdatalab.utils.SecureTokenStorage
import java.util.UUID

interface UserRepository {
    fun cargarUsuarios(): List<Usuario>
    fun registrarUsuario(
        alias: String,
        email: String,
        password: String,
        consentimientoAceptado: Boolean
    ): Pair<Boolean, String>
    fun validarLogin(emailOrAlias: String, password: String): Usuario?
    fun getSesionActual(): String?
    fun guardarSesionActual(alias: String?)
    fun getUsuarioActual(alias: String?): Usuario?
}

class PreferencesUserRepository(context: Context) : UserRepository {
    private val appContext = context.applicationContext

    override fun cargarUsuarios(): List<Usuario> = LocalPreferencesStorage.cargarUsuarios(appContext)

    override fun registrarUsuario(
        alias: String,
        email: String,
        password: String,
        consentimientoAceptado: Boolean
    ): Pair<Boolean, String> {
        val aliasTrim = alias.trim()
        val emailTrim = email.trim().lowercase()
        if (!consentimientoAceptado) {
            return Pair(false, "Debes aceptar el consentimiento informado para poder registrarte.")
        }
        if (aliasTrim.length < 3) return Pair(false, "El alias debe tener al menos 3 caracteres")
        if (!emailTrim.contains("@") || !emailTrim.contains(".")) return Pair(false, "Correo electrónico inválido")
        if (password.length < 4) return Pair(false, "La contraseña debe tener al menos 4 caracteres")

        val usuarios = cargarUsuarios().toMutableList()
        if (usuarios.any { it.alias.equals(aliasTrim, ignoreCase = true) }) {
            return Pair(false, "El alias '$aliasTrim' ya está en uso")
        }
        if (usuarios.any { it.email.equals(emailTrim, ignoreCase = true) }) {
            return Pair(false, "El correo '$emailTrim' ya está registrado")
        }

        val nuevoUsuario = Usuario(
            alias = aliasTrim,
            email = emailTrim,
            passwordHash = password,
            consentimientoAceptado = true,
            fechaConsentimiento = System.currentTimeMillis(),
            versionConsentimiento = ConsentimientoInfo.VERSION
        )
        usuarios.add(nuevoUsuario)
        LocalPreferencesStorage.guardarUsuarios(appContext, usuarios)
        LocalPreferencesStorage.guardarSesionUsuario(appContext, aliasTrim)

        // Generar y almacenar token cifrado en Android KeyStore
        val secureToken = "token_" + UUID.randomUUID().toString()
        SecureTokenStorage.guardarTokenYSesion(appContext, aliasTrim, secureToken)

        return Pair(true, "Usuario registrado exitosamente")
    }

    override fun validarLogin(emailOrAlias: String, password: String): Usuario? {
        val query = emailOrAlias.trim().lowercase()
        val usuario = cargarUsuarios().find {
            (it.alias.lowercase() == query || it.email.lowercase() == query) && it.passwordHash == password
        }
        if (usuario != null) {
            LocalPreferencesStorage.guardarSesionUsuario(appContext, usuario.alias)
            val secureToken = "token_" + UUID.randomUUID().toString()
            SecureTokenStorage.guardarTokenYSesion(appContext, usuario.alias, secureToken)
        }
        return usuario
    }

    override fun getSesionActual(): String? {
        val aliasStorage = LocalPreferencesStorage.cargarSesionUsuario(appContext)
        val aliasSecure = SecureTokenStorage.obtenerAlias(appContext)
        return aliasSecure ?: aliasStorage
    }

    override fun guardarSesionActual(alias: String?) {
        LocalPreferencesStorage.guardarSesionUsuario(appContext, alias)
        if (alias == null) {
            SecureTokenStorage.borrarTokenYSesion(appContext)
        }
    }

    override fun getUsuarioActual(alias: String?): Usuario? {
        if (alias.isNullOrBlank()) return null
        return cargarUsuarios().find { it.alias.equals(alias, ignoreCase = true) }
    }
}

class InMemoryUserRepository(
    private var usuarios: MutableList<Usuario> = mutableListOf(
        Usuario(
            alias = "Usuario_Prueba_01",
            email = "prueba@lemac.cl",
            passwordHash = "1234",
            consentimientoAceptado = true,
            fechaConsentimiento = System.currentTimeMillis(),
            versionConsentimiento = ConsentimientoInfo.VERSION
        )
    ),
    private var sesionActual: String? = "Usuario_Prueba_01"
) : UserRepository {
    override fun cargarUsuarios(): List<Usuario> = usuarios

    override fun registrarUsuario(
        alias: String,
        email: String,
        password: String,
        consentimientoAceptado: Boolean
    ): Pair<Boolean, String> {
        val aliasTrim = alias.trim()
        val emailTrim = email.trim().lowercase()
        if (!consentimientoAceptado) {
            return Pair(false, "Debes aceptar el consentimiento informado para poder registrarte.")
        }
        if (aliasTrim.length < 3) return Pair(false, "El alias debe tener al menos 3 caracteres")
        if (!emailTrim.contains("@")) return Pair(false, "Correo electrónico inválido")
        if (password.length < 4) return Pair(false, "La contraseña debe tener al menos 4 caracteres")

        if (usuarios.any { it.alias.equals(aliasTrim, ignoreCase = true) }) {
            return Pair(false, "El alias '$aliasTrim' ya está en uso")
        }
        val nuevo = Usuario(
            alias = aliasTrim,
            email = emailTrim,
            passwordHash = password,
            consentimientoAceptado = true,
            fechaConsentimiento = System.currentTimeMillis(),
            versionConsentimiento = ConsentimientoInfo.VERSION
        )
        usuarios.add(nuevo)
        sesionActual = aliasTrim
        return Pair(true, "Usuario registrado exitosamente")
    }

    override fun validarLogin(emailOrAlias: String, password: String): Usuario? {
        val query = emailOrAlias.trim().lowercase()
        val user = usuarios.find {
            (it.alias.lowercase() == query || it.email.lowercase() == query) && it.passwordHash == password
        }
        if (user != null) sesionActual = user.alias
        return user
    }

    override fun getSesionActual(): String? = sesionActual

    override fun guardarSesionActual(alias: String?) { sesionActual = alias }

    override fun getUsuarioActual(alias: String?): Usuario? {
        if (alias.isNullOrBlank()) return null
        return usuarios.find { it.alias.equals(alias, ignoreCase = true) }
    }
}
