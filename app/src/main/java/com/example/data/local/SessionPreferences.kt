package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "bocchi_session_prefs")

data class StoredSession(
    val username: String,
    val accessToken: String,
    val refreshToken: String?
)

class SessionPreferences(private val context: Context) {

    private val keyUsername = stringPreferencesKey("current_username")
    private val keyAccessToken = stringPreferencesKey("access_token")
    private val keyRefreshToken = stringPreferencesKey("refresh_token")

    val sessionFlow: Flow<StoredSession?> = context.dataStore.data.map { prefs ->
        val username = prefs[keyUsername]
        val access = prefs[keyAccessToken]
        if (!username.isNullOrBlank() && !access.isNullOrBlank()) {
            StoredSession(
                username = username,
                accessToken = access,
                refreshToken = prefs[keyRefreshToken]
            )
        } else {
            null
        }
    }

    suspend fun saveSession(username: String, accessToken: String, refreshToken: String?) {
        context.dataStore.edit { prefs ->
            prefs[keyUsername] = username
            prefs[keyAccessToken] = accessToken
            if (!refreshToken.isNullOrBlank()) {
                prefs[keyRefreshToken] = refreshToken
            } else {
                prefs.remove(keyRefreshToken)
            }
        }
    }

    suspend fun updateAccessToken(accessToken: String) {
        context.dataStore.edit { prefs ->
            prefs[keyAccessToken] = accessToken
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(keyUsername)
            prefs.remove(keyAccessToken)
            prefs.remove(keyRefreshToken)
        }
    }
}
