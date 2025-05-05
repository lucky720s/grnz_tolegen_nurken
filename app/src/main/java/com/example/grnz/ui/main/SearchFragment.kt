package com.example.grnz.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grnz.R
import com.example.grnz.data.network.model.Photo
import com.example.grnz.util.EventObserver

class SearchFragment : Fragment() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var noResultsTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var numberEditText: EditText
    private lateinit var checkButton: Button
    private lateinit var vehicleInfoLabel: TextView
    private lateinit var vehicleCarTextView: TextView
    private lateinit var vehicleYearTextView: TextView
    private lateinit var clearHistoryButton: ImageButton
    private lateinit var historyLabel: TextView

    private val viewModel: SearchViewModel by viewModels()

    private lateinit var historyAdapter: SearchHistoryAdapter
    private lateinit var resultsAdapter: SearchResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        numberEditText = view.findViewById(R.id.edit_text_number)
        checkButton = view.findViewById(R.id.button_check)
        historyRecyclerView = view.findViewById(R.id.recycler_view_history)
        resultsRecyclerView = view.findViewById(R.id.recycler_view_results)
        noResultsTextView = view.findViewById(R.id.text_view_no_results)
        progressBar = view.findViewById(R.id.progress_bar)
        vehicleInfoLabel = view.findViewById(R.id.text_view_vehicle_info_label)
        vehicleCarTextView = view.findViewById(R.id.text_view_vehicle_car)
        vehicleYearTextView = view.findViewById(R.id.text_view_vehicle_year)
        clearHistoryButton = view.findViewById(R.id.button_clear_history)
        historyLabel = view.findViewById(R.id.text_view_history_label)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        historyAdapter = SearchHistoryAdapter(emptyList()) { clickedGrnz ->
            numberEditText.setText(clickedGrnz)
            viewModel.searchNumber(clickedGrnz)
        }
        historyRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        historyRecyclerView.adapter = historyAdapter
        resultsAdapter = SearchResultAdapter(emptyList()) { userId ->
            val action = SearchFragmentDirections.actionSearchFragmentToUserPhotosFragment(userId)
            findNavController().navigate(action)
        }
        resultsRecyclerView.layoutManager = LinearLayoutManager(context)
        resultsRecyclerView.adapter = resultsAdapter
    }

    private fun setupClickListeners() {
        checkButton.setOnClickListener {
            val number = numberEditText.text.toString().trim()
            if (number.isNotEmpty()) {
                viewModel.searchNumber(number)
            } else {
                Toast.makeText(context, R.string.search_toast_enter_number, Toast.LENGTH_SHORT).show()
            }
        }
        clearHistoryButton.setOnClickListener {
            showClearHistoryConfirmationDialog()
        }
    }

    private fun showClearHistoryConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.search_clear_history_dialog_title)
            .setMessage(R.string.search_clear_history_dialog_message)
            .setPositiveButton(R.string.search_clear_history_dialog_positive) { dialog, _ ->
                viewModel.clearSearchHistory()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.search_clear_history_dialog_negative) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            progressBar.isVisible = state is SearchUiState.Loading
            vehicleInfoLabel.isVisible = false
            vehicleCarTextView.isVisible = false
            vehicleYearTextView.isVisible = false
            resultsRecyclerView.isVisible = false
            noResultsTextView.isVisible = false

            when (state) {
                is SearchUiState.Success -> {
                    if (state.vehicle != null) {
                        vehicleInfoLabel.isVisible = true
                        vehicleCarTextView.isVisible = !state.vehicle.car.isNullOrBlank()
                        vehicleCarTextView.text = state.vehicle.car ?: ""
                        vehicleYearTextView.isVisible = !state.vehicle.year.isNullOrBlank()
                        vehicleYearTextView.text = state.vehicle.year ?: ""
                    }
                    if (state.photos.isNotEmpty()) {
                        resultsRecyclerView.isVisible = true
                        resultsAdapter.updateData(state.photos)
                    } else {
                        noResultsTextView.isVisible = true
                        noResultsTextView.text = getString(R.string.search_no_results_found)
                        resultsAdapter.updateData(emptyList())
                    }
                }
                is SearchUiState.Error -> {
                    resultsAdapter.updateData(emptyList())
                    noResultsTextView.isVisible = true
                    noResultsTextView.text = state.message
                }
                is SearchUiState.Loading -> {}
                is SearchUiState.Idle -> {
                    resultsAdapter.updateData(emptyList())
                }
            }
        }

        viewModel.searchHistory.observe(viewLifecycleOwner) { historyList ->
            val isHistoryVisible = historyList.isNotEmpty()
            historyLabel.isVisible = isHistoryVisible
            historyRecyclerView.isVisible = isHistoryVisible
            clearHistoryButton.isVisible = isHistoryVisible
            historyAdapter.updateData(historyList)
        }

        viewModel.toastEvent.observe(viewLifecycleOwner, EventObserver { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        })
    }
}
