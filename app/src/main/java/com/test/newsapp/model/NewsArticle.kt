package com.test.newsapp.model

data class NewsArticle(
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val name: String?
)

data class NewsResponse(
    val articles: List<NewsArticle>
)