package com.example.lemacdatalab.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Almacenamiento seguro en Android KeyStore usando EncryptedSharedPreferences.
 * Garantiza que los tokens de autenticación estén cifrados en hardware.
 */
object SecureTokenStorage {

    private const val PREFS_FILENAME = "lemac_secure_tokens"
    private const val KEY_AUTH_TOKEN = "auth_token_key"
    private const val KEY_USER_ALIAS = "secure_alias_key"

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun guardarTokenYSesion(context: Context, alias: String, token: String) {
        getEncryptedPrefs(context).edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_USER_ALIAS, alias)
            .apply()
    }

    fun obtenerToken(context: Context): String? {
        return getEncryptedPrefs(context).getString(KEY_AUTH_TOKEN, null)
    }

    fun obtenerAlias(context: Context): String? {
        return getEncryptedPrefs(context).getString(KEY_USER_ALIAS, null)
    }

    fun borrarTokenYSesion(context: Context) {
        getEncryptedPrefs(context).edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ALIAS)
            .apply()
    }
}
