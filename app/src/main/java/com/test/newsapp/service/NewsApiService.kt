package com.test.newsapp.service

import com.test.newsapp.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface NewsApiService {
    @GET("everything?q=apple&from=2025-02-21&to=2025-02-21&sortBy=popularity&apiKey=fd924131deaa43798199f544728cd309")
    fun getTopHeadlines(): Call<NewsResponse>

    @GET("everything?q=apple&from=2025-02-21&to=2025-02-21&sortBy=popularity&apiKey=fd924131deaa43798199f544728cd309")
    fun searchNews(@Query("q") query: String): Call<NewsResponse>
}