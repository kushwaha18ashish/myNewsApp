package com.mjolnir.newsapp.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mjolnir.newsapp.repository.NewsRepository

class NewsViewModelProviderFactory(val app:Application,val newsRepository: NewsRepository):ViewModelProvider.Factory {
    override fun <T:ViewModel> create(modelClass: Class<T>):T{
        return NewsViewModel(app,newsRepository) as T
    }
}