package com.test.newsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.test.newsapp.adapter.NewsAdapter
import com.test.newsapp.viewmodel.NewsViewModel
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var viewModel: NewsViewModel
    private lateinit var searchView: SearchView

    private val searchJob: CompletableJob = Job()
    private val searchScope = CoroutineScope(Dispatchers.Main + searchJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout)
        searchView = findViewById(R.id.searchView)

        // Initialize ViewModel using a Factory
        val factory = NewsViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory).get(NewsViewModel::class.java)

        // Initialize GridLayoutManager
        val gridLayoutManager = GridLayoutManager(this, 2) // 2 columns
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if ((position + 4) % 5 == 0) { // Every 5th item takes 2 columns
                    2
                } else {
                    1
                }
            }
        }
        recyclerView.layoutManager = gridLayoutManager

        // Initialize adapter
        adapter = NewsAdapter(this@MainActivity, emptyList(), { newsArticle ->
            // Handle item click here
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newsArticle.url))
            startActivity(intent)
            viewModel.saveReadNewsUrl(newsArticle.url ?: "")
        }, viewModel)
        recyclerView.adapter = adapter

        // Observe LiveData from ViewModel
        viewModel.newsList.observe(this, Observer { newsList ->
            adapter.setNewsList(newsList) // Update the news list
        })

        // Observe readNewsUrls LiveData
        viewModel.readNewsUrls.observe(this, Observer { readNewsUrls ->
            adapter.setReadNewsUrls(readNewsUrls)
        })

        // Observe Loading State
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                shimmerFrameLayout.visibility = View.VISIBLE
                shimmerFrameLayout.startShimmer()
                recyclerView.visibility = View.GONE
            } else {
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        })

        // Observe Error Message
        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        })

        // Setup Search View
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchNews(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Cancel the previous job
                searchJob.cancel()

                // Start a new job
                val newJob = Job()
                searchScope.launch(newJob) {
                    delay(300) // Debounce the search by 300ms
                    newText?.let {
                        withContext(Dispatchers.Main) {
                            viewModel.searchNews(it)
                        }
                    }
                }
                searchJob.complete()
                return true
            }
        })

        // Fetch news data
        viewModel.fetchNews()
    }

    override fun onResume() {
        super.onResume()
        // Shimmer is controlled by ViewModel's isLoading LiveData now
        //shimmerFrameLayout.startShimmer()
    }

    override fun onPause() {
        // Shimmer is controlled by ViewModel's isLoading LiveData now
        //shimmerFrameLayout.stopShimmer()
        super.onPause()
    }
}

// Create a Factory for NewsViewModel
class NewsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}