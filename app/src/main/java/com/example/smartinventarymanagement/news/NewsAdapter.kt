package com.example.smartinventarymanagement.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartinventarymanagement.R

class NewsAdapter(
    private val articles: List<Article>,
    private val onItemClick: (Article) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView = view.findViewById(R.id.imgNews)

        val title: TextView = view.findViewById(R.id.tvTitle)

        val description: TextView = view.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {

        val view = LayoutInflater.from(parent.context)

            .inflate(R.layout.item_news, parent, false)

        return NewsViewHolder(view)
    }

    override fun getItemCount(): Int {

        return articles.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {

        val article = articles[position]

        holder.title.text = article.title

        holder.description.text = article.description

        Glide.with(holder.itemView.context)
            .load(article.urlToImage)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.image)

        holder.itemView.setOnClickListener {
            onItemClick(article)
        }
    }
}