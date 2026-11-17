package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studioDataStore by preferencesDataStore(name = "studio_session_prefs")

class SessionPreferences(private val context: Context) {
    private object Keys {
        val ACTIVE_USER_ID = longPreferencesKey("active_user_id")
        val SIGNUP_FREE_CREDITS = intPreferencesKey("signup_free_credits")
        val DAILY_BONUS_CREDITS = intPreferencesKey("daily_bonus_credits")
        val MAINTENANCE_MODE = booleanPreferencesKey("maintenance_mode")
    }

    val activeUserId: Flow<Long?> = context.studioDataStore.data.map { prefs ->
        val id = prefs[Keys.ACTIVE_USER_ID] ?: -1L
        if (id > 0L) id else null
    }

    val signupFreeCredits: Flow<Int> = context.studioDataStore.data.map { prefs ->
        prefs[Keys.SIGNUP_FREE_CREDITS] ?: 25
    }

    val dailyBonusCredits: Flow<Int> = context.studioDataStore.data.map { prefs ->
        prefs[Keys.DAILY_BONUS_CREDITS] ?: 5
    }

    val maintenanceMode: Flow<Boolean> = context.studioDataStore.data.map { prefs ->
        prefs[Keys.MAINTENANCE_MODE] ?: false
    }

    suspend fun setActiveUserId(userId: Long?) {
        context.studioDataStore.edit { prefs ->
            if (userId == null) prefs.remove(Keys.ACTIVE_USER_ID)
            else prefs[Keys.ACTIVE_USER_ID] = userId
        }
    }

    suspend fun updateAppSettings(signupFreeCredits: Int, dailyBonusCredits: Int, maintenanceMode: Boolean) {
        context.studioDataStore.edit { prefs ->
            prefs[Keys.SIGNUP_FREE_CREDITS] = signupFreeCredits.coerceIn(0, 500)
            prefs[Keys.DAILY_BONUS_CREDITS] = dailyBonusCredits.coerceIn(0, 100)
            prefs[Keys.MAINTENANCE_MODE] = maintenanceMode
        }
    }
}
