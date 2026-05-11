package com.example.smartinventarymanagement.news

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("v2/everything")
    fun getNews(

        @Query("q") query: String,

        @Query("sortBy") sortBy: String,

        @Query("apiKey") apiKey: String

    ): Call<NewsResponse>
}