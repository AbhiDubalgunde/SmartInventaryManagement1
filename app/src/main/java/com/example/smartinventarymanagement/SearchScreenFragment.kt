package com.example.smartinventarymanagement

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartinventarymanagement.news.Article
import com.example.smartinventarymanagement.news.NewsAdapter
import com.example.smartinventarymanagement.news.NewsResponse
import com.example.smartinventarymanagement.news.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchScreenFragment : Fragment() {

    private lateinit var recyclerNews: RecyclerView
    private lateinit var etNewsSearch: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_search_screen,
            container,
            false
        )

        recyclerNews = view.findViewById(R.id.recyclerNews)
        etNewsSearch = view.findViewById(R.id.etNewsSearch)

        recyclerNews.layoutManager = LinearLayoutManager(requireContext())

        etNewsSearch.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                val searchText = etNewsSearch.text.toString()

                searchNews(searchText)

                true

            } else {

                false
            }
        }
        getNews()

        return view
    }

    private fun getNews() {

        RetrofitInstance.api.getNews(
            "technology",
            "publishedAt",
            "533c562aa2b84190ba72c594e9b6394b"
        ).enqueue(object : Callback<NewsResponse> {

            override fun onResponse(
                call: Call<NewsResponse>,
                response: Response<NewsResponse>
            ) {
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    
                    recyclerNews.adapter = NewsAdapter(articles) { article ->
                        openNewsArticle(article.url)
                    }

                } else {
                    android.util.Log.d("NEWS_DEBUG", "API ERROR = ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                android.util.Log.d("NEWS_DEBUG", "FAILED = ${t.message}")
            }
        })
    }

    private fun searchNews(query: String) {

        RetrofitInstance.api.getNews(
            query,
            "publishedAt",
            "533c562aa2b84190ba72c594e9b6394b"
        ).enqueue(object : Callback<NewsResponse> {

            override fun onResponse(
                call: Call<NewsResponse>,
                response: Response<NewsResponse>
            ) {
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()

                    recyclerNews.adapter = NewsAdapter(articles) { article ->
                        openNewsArticle(article.url)
                    }
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun openNewsArticle(url: String) {
        try {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
