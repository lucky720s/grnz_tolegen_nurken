package com.example.grnz.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.grnz.R

class FeedFragment : Fragment() {

    private val viewModel: FeedViewModel by viewModels()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var errorTextView: TextView
    private lateinit var numberFilterEditText: EditText
    private lateinit var lettersFilterEditText: EditText
    private lateinit var regionFilterEditText: EditText
    private lateinit var applyFilterButton: Button
    private lateinit var feedAdapter: SearchResultAdapter
    private var canLoadMore = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feed, container, false)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        recyclerView = view.findViewById(R.id.recycler_view_feed)
        progressBar = view.findViewById(R.id.progress_bar_feed)
        emptyTextView = view.findViewById(R.id.text_view_feed_empty)
        errorTextView = view.findViewById(R.id.text_view_feed_error)
        numberFilterEditText = view.findViewById(R.id.edit_text_filter_number)
        lettersFilterEditText = view.findViewById(R.id.edit_text_filter_letters)
        regionFilterEditText = view.findViewById(R.id.edit_text_filter_region)
        applyFilterButton = view.findViewById(R.id.button_apply_filter)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToRefresh()
        setupFilterButton()
        observeViewModel()
        if (viewModel.uiState.value == null) {
            viewModel.loadInitialFeed()
        }
    }

    private fun setupRecyclerView() {
        feedAdapter = SearchResultAdapter(emptyList()) { userId ->
            val action = FeedFragmentDirections.actionFeedFragmentToUserPhotosFragment(userId)
            findNavController().navigate(action)
        }
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = feedAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val currentState = viewModel.uiState.value
                if (canLoadMore &&
                    !viewModel.isLoading &&
                    currentState !is FeedUiState.Loading &&
                    currentState !is FeedUiState.LoadingMore &&
                    (visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                    firstVisibleItemPosition >= 0 && totalItemCount > 0
                ) {
                    viewModel.loadMoreFeed()
                }
            }
        })
    }

    private fun setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            numberFilterEditText.text.clear()
            lettersFilterEditText.text.clear()
            regionFilterEditText.text.clear()
            viewModel.loadInitialFeed()
        }
    }

    private fun setupFilterButton() {
        applyFilterButton.setOnClickListener {
            val number = numberFilterEditText.text.toString()
            val letters = lettersFilterEditText.text.toString()
            val region = regionFilterEditText.text.toString()
            viewModel.applyFilters(number, letters, region)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            swipeRefreshLayout.isRefreshing = state is FeedUiState.Loading && state.isRefresh
            progressBar.isVisible = state is FeedUiState.Loading && state.isRefresh
            recyclerView.isVisible = state is FeedUiState.Success || state is FeedUiState.LoadingMore
            emptyTextView.isVisible = state is FeedUiState.Empty
            errorTextView.isVisible = state is FeedUiState.Error
            when (state) {
                is FeedUiState.Loading -> {
                }
                is FeedUiState.Success -> {
                    feedAdapter.updateData(state.photos)
                    canLoadMore = state.canLoadMore
                    progressBar.isVisible = false
                }
                is FeedUiState.Error -> {
                    errorTextView.text = state.message
                    canLoadMore = false
                    progressBar.isVisible = false
                }
                is FeedUiState.Empty -> {
                    feedAdapter.updateData(emptyList())
                    canLoadMore = false
                    progressBar.isVisible = false
                }
                is FeedUiState.LoadingMore -> {
                    progressBar.isVisible = false
                }
            }
        }
    }
}
