package com.zenflow.brain.detox.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserRepository(private val context: Context) {

    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val IS_GUEST = booleanPreferencesKey("is_guest")
        private val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN] }
    val isGuest: Flow<Boolean> = context.dataStore.data.map { it[IS_GUEST] ?: false }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL] }

    suspend fun saveAuthToken(token: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_TOKEN] = token
            prefs[USER_EMAIL] = email
            prefs[IS_GUEST] = false
        }
    }

    suspend fun setGuestMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_GUEST] = enabled
            if (enabled) {
                prefs.remove(AUTH_TOKEN)
                prefs.remove(USER_EMAIL)
            }
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs.remove(AUTH_TOKEN)
            prefs.remove(USER_EMAIL)
            prefs[IS_GUEST] = false
        }
    }

    suspend fun isAuthenticated(): Boolean {
        return context.dataStore.data.map { 
            it[AUTH_TOKEN] != null || it[IS_GUEST] == true 
        }.firstOrNull() ?: false
    }
}
