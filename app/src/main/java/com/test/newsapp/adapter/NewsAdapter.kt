package com.test.newsapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.test.newsapp.R
import com.test.newsapp.model.NewsArticle
import com.test.newsapp.viewmodel.NewsViewModel
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(
    private val context: Context,
    private var newsList: List<NewsArticle>,
    private val onItemClick: (NewsArticle) -> Unit,
    private val viewModel: NewsViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_LARGE = 0
    private val TYPE_SMALL = 1
    private var readNewsUrls: Set<String> = emptySet()

    fun setNewsList(list: List<NewsArticle>) {
        newsList = list
        notifyDataSetChanged()
    }

    fun setReadNewsUrls(urls: Set<String>) {
        readNewsUrls = urls
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position + 4) % 5 == 0) { // Determining the position of major news items
            TYPE_LARGE
        } else {
            TYPE_SMALL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_LARGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.news_item_large, parent, false)
                LargeNewsViewHolder(view)
            }
            TYPE_SMALL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.news_item_small, parent, false)
                SmallNewsViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val news = newsList[position]

        when (holder) {
            is LargeNewsViewHolder -> {
                holder.bind(news)
                holder.itemView.setOnClickListener { onItemClick(news) }
                // Load image with Glide
                Glide.with(context)
                    .load(news.urlToImage)
                    .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                    .error(R.drawable.ic_launcher_foreground) // Error image
                    .into(holder.newsImageView)

                // Check if news has been read using ViewModel
                if (viewModel.isNewsRead(news.url ?: "")) {
                    // Change background color or add a read icon
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.read_news_background))
                } else {
                    // Reset background color
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                }
            }
            is SmallNewsViewHolder -> {
                holder.bind(news)
                holder.itemView.setOnClickListener { onItemClick(news) }
                // Load image with Glide
                Glide.with(context)
                    .load(news.urlToImage)
                    .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                    .error(R.drawable.ic_launcher_foreground) // Error image
                    .into(holder.newsImageView)
                // Check if news has been read using ViewModel
                if (viewModel.isNewsRead(news.url ?: "")) {
                    // Change background color or add a read icon
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.read_news_background))
                } else {
                    // Reset background color
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    inner class LargeNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val newsImageView: ImageView = itemView.findViewById(R.id.newsImageView)
        val publishedAtTextView: TextView = itemView.findViewById(R.id.publishedAtTextView)

        fun bind(news: NewsArticle) {
            titleTextView.text = news.title ?: "No Title"
            authorTextView.text = news.author ?: "Unknown Author"
            descriptionTextView.text = news.description ?: "No Description"

            // Format publishedAt
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEE, dd MMM HH.mm", Locale.getDefault()) // Sen, 25 April 20.34
                val date = news.publishedAt?.let { inputFormat.parse(it) }
                publishedAtTextView.text = outputFormat.format(date!!)
            } catch (e: Exception) {
                publishedAtTextView.text = news.publishedAt ?: "Unknown Date"
            }
        }
    }

    inner class SmallNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
        val newsImageView: ImageView = itemView.findViewById(R.id.newsImageView)
        val publishedAtTextView: TextView = itemView.findViewById(R.id.publishedAtTextView)

        fun bind(news: NewsArticle) {
            titleTextView.text = news.title ?: "No Title"
            authorTextView.text = news.author ?: "Unknown Author"

            // Format publishedAt
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEE, dd MMM HH.mm", Locale.getDefault()) // Sen, 25 April 20.34
                val date = news.publishedAt?.let { inputFormat.parse(it) }
                publishedAtTextView.text = outputFormat.format(date!!)
            } catch (e: Exception) {
                publishedAtTextView.text = news.publishedAt ?: "Unknown Date"
            }
        }
    }
}