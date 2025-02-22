package com.test.newsapp.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import android.util.Log
import com.test.newsapp.datastore.ReadNewsManager
import com.test.newsapp.model.NewsArticle
import com.test.newsapp.`object`.RetrofitInstance

class NewsViewModel(private val context: Context) : ViewModel() {

    val newsList: MutableLiveData<List<NewsArticle>> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val errorMessage: MutableLiveData<String?> = MutableLiveData(null)
    private val readNewsManager = ReadNewsManager(context)
    val readNewsUrls: MutableLiveData<Set<String>> = MutableLiveData(emptySet())


    init {
        // Observe read news URLs when the ViewModel is created
        observeReadNewsUrls()
    }


    fun fetchNews() {
        isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getTopHeadlines().execute()

                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    if (response.isSuccessful) {
                        newsList.value = response.body()?.articles ?: emptyList()
                        errorMessage.value = null
                    } else {
                        Log.e("com.test.newsapp.viewmodel.NewsViewModel", "Error fetching news: ${response.message()}")
                        errorMessage.value = "Failed to fetch news: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                Log.e("com.test.newsapp.viewmodel.NewsViewModel", "Exception during news fetch: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    errorMessage.value = "Network error occurred: ${e.message}"
                }
            }
        }
    }


    fun searchNews(query: String) {
        isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.searchNews(query).execute()

                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    if (response.isSuccessful) {
                        newsList.value = response.body()?.articles ?: emptyList()
                        errorMessage.value = null
                    } else {
                        Log.e("com.test.newsapp.viewmodel.NewsViewModel", "Error searching news: ${response.message()}")
                        errorMessage.value = "Failed to search news: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                Log.e("com.test.newsapp.viewmodel.NewsViewModel", "Exception during news search: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    errorMessage.value = "Network error occurred: ${e.message}"
                }
            }
        }
    }

    // Function to save a read news URL
    fun saveReadNewsUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            readNewsManager.saveReadNewsUrl(url)
        }
    }

    // Observe read news URLs from DataStore
    private fun observeReadNewsUrls() {
        viewModelScope.launch {
            readNewsManager.readNewsUrls.collectLatest { urls ->
                readNewsUrls.value = urls
                Log.d("com.test.newsapp.viewmodel.NewsViewModel", "Read news URLs updated: $urls") // Log the URLs
            }
        }
    }

    // Function to check if a news URL has been read
    fun isNewsRead(url: String): Boolean {
        return readNewsUrls.value?.contains(url) == true
    }
}