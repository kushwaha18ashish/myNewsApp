package com.mjolnir.newsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.newsprojectpractice.R
import com.example.newsprojectpractice.databinding.FragmentHeadlineBinding
import com.mjolnir.newsapp.adapters.NewsAdapter
import com.mjolnir.newsapp.ui.NewsActivity
import com.mjolnir.newsapp.ui.NewsViewModel
import com.mjolnir.newsapp.util.Constants
import com.mjolnir.newsapp.util.Resource
import org.w3c.dom.Text


class HeadlineFragment : Fragment(R.layout.fragment_headline) {

    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var retryButton: Button
    lateinit var errorText:Text
    lateinit var itemHeadsError:CardView
    lateinit var binding: FragmentHeadlineBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentHeadlineBinding.bind(view)

        itemHeadsError= view.findViewById(R.id.itemHeadlinesError)
        val inflater= requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view:View= inflater.inflate(R.layout.item_error,null)

        retryButton=view.findViewById(R.id.retryButton)
        errorText=view.findViewById(R.id.errorText)

        newsViewModel = (activity as NewsActivity).newsViewModel
        setupHeadlinesRecycler()

        newsAdapter.setOnClickListener {
            val bundle= Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(R.id.action_headlineFragment2_to_articleFragment,bundle)
        }

        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is Resource.Success<*> ->{
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let {newsResponse->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages= newsResponse.totalResults / Constants.QUERY_PAGE_SIZE+2
                        isLastPage= newsViewModel.headlinesPage == totalPages
                        if(isLastPage){
                            binding.recyclerHeadlines.setPadding(0,0,0,0)
                        }
                    }
                }

                is Resource.Error<*> ->{
                    hideProgressBar()
                    response.message?.let { message->
                        Toast.makeText(activity,"Sorry, error: $message",Toast.LENGTH_SHORT).show()
                        showErrorMessage(message)
                    }
                }

                is Resource.Loading<*> ->{
                    showProgressBar()
                }
            }
        })

        retryButton.setOnClickListener {
            newsViewModel.getHeadlines("in")
        }
    }

    var isError=false
    var isLoading=false
    var isLastPage=false
    var isScrolling=false

    private fun hideProgressBar(){
        binding.paginationProgressBar.visibility=View.INVISIBLE
        isLoading=false
    }

    private fun showProgressBar(){
        binding.paginationProgressBar.visibility=View.VISIBLE
        isLoading=true

    }

    private fun hideErrorMessage(){
        itemHeadsError.visibility=View.INVISIBLE
        isError=false
    }

    private fun showErrorMessage(message:String){
        itemHeadsError.visibility=View.VISIBLE
        errorText.textContent=message
        isLoading=true
    }

    val scrollListener=object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager= recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition=layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount=layoutManager.childCount
            val totalItemCount=layoutManager.itemCount

            val isNoErrors= !isError
            val isNotLoadingAndNotLastPage= !isLoading && !isLastPage
            val isAtLastItem= firstVisibleItemPosition+visibleItemCount >= totalItemCount
            val isNotAtBeginning= firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible= totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if(shouldPaginate){
                newsViewModel.getHeadlines("in")
                isScrolling=false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling=true
            }
        }
    }
    private fun setupHeadlinesRecycler(){
        newsAdapter=NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@HeadlineFragment.scrollListener)
        }

    }
}