package com.mjolnir.newsapp.api

import com.mjolnir.newsapp.models.NewsResponse
import com.mjolnir.newsapp.util.Constants.Companion.API_KEY
import okhttp3.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale.IsoCountryCode

interface NewsAPI {
    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("country")
        countryCode: String="in",
        @Query("page")
        pageNumber:Int=1,
        @Query("apiKey")
        apiKey:String=API_KEY

    ): retrofit2.Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery:String,
        @Query("page")
        pageNumber: Int=1,
        @Query("apiKey")
        apiKey:String=API_KEY
    ):retrofit2.Response<NewsResponse>
}