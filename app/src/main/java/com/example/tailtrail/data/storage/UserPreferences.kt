package com.example.tailtrail.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Class that handles storing and retrieving user data using DataStore
 */
class UserPreferences(private val context: Context) {

    companion object {
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val IS_QUIZ_KEY = intPreferencesKey("is_quiz")
    }

    // Get the stored user ID
    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    // Get the stored user name
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    // Get the stored isQuiz value
    val isQuiz: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[IS_QUIZ_KEY]
    }

    // Save user information to DataStore
    suspend fun saveUserInfo(userId: Int, userName: String, isQuiz: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_NAME_KEY] = userName
            preferences[IS_QUIZ_KEY] = isQuiz
        }
    }

    // Update isQuiz value
    suspend fun setQuizTaken(isQuiz: Int) {
        context.dataStore.edit { preferences ->
            preferences[IS_QUIZ_KEY] = isQuiz
        }
    }

    // Clear all stored user data
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
