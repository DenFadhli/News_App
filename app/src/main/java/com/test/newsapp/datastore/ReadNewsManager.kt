package com.test.newsapp.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "read_news")

class ReadNewsManager(private val context: Context) {

    private val READ_NEWS_URLS_KEY = stringSetPreferencesKey("read_news_urls")

    // Function to save a URL to the read news list
    suspend fun saveReadNewsUrl(url: String) {
        context.dataStore.edit { preferences ->
            val currentUrls = preferences[READ_NEWS_URLS_KEY] ?: emptySet()
            preferences[READ_NEWS_URLS_KEY] = currentUrls + url
        }
    }

    // Function to get the list of read news URLs
    val readNewsUrls: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[READ_NEWS_URLS_KEY] ?: emptySet()
        }

    // Function to clear all read news URLs
    suspend fun clearReadNewsUrls() {
        context.dataStore.edit { preferences ->
            preferences.remove(READ_NEWS_URLS_KEY)
        }
    }
}