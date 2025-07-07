package com.example.tailtrail.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userDataStore by preferencesDataStore(name = "user_prefs")

object UserPreferencesKeys {
    val USER_ID = intPreferencesKey("user_id")
}

class UserPreferences(private val context: Context) {
    val userIdFlow: Flow<Int?> = context.userDataStore.data.map { prefs ->
        prefs[UserPreferencesKeys.USER_ID]
    }

    suspend fun saveUserId(userId: Int) {
        context.userDataStore.edit { prefs ->
            prefs[UserPreferencesKeys.USER_ID] = userId
        }
    }

    suspend fun clearUserId() {
        context.userDataStore.edit { prefs ->
            prefs.remove(UserPreferencesKeys.USER_ID)
        }
    }
}

