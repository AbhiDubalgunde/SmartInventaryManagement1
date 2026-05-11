package com.example.smartinventarymanagement.news

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api: NewsApi by lazy {

        Retrofit.Builder()

            .baseUrl("https://newsapi.org/")

            .addConverterFactory(GsonConverterFactory.create())

            .build()

            .create(NewsApi::class.java)
    }
}