package com.example.grnz.ui.userphotos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grnz.R
import com.example.grnz.ui.main.SearchResultAdapter

class UserPhotosFragment : Fragment() {

    private lateinit var userEmailTextView: TextView
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noPhotosTextView: TextView

    private val viewModel: UserPhotosViewModel by viewModels()
    private val args: UserPhotosFragmentArgs by navArgs()

    private lateinit var photosAdapter: SearchResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_photos, container, false)
        userEmailTextView = view.findViewById(R.id.text_view_user_email)
        photosRecyclerView = view.findViewById(R.id.recycler_view_user_photos)
        progressBar = view.findViewById(R.id.progress_bar_user_photos)
        noPhotosTextView = view.findViewById(R.id.text_view_no_user_photos)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        viewModel.loadUserPhotos(args.userId)
    }

    private fun setupRecyclerView() {
        photosAdapter = SearchResultAdapter(emptyList()) { userId ->
            Log.d("UserPhotosFragment", "Uploader info clicked for userId: $userId, no action needed.")
        }
        photosRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        photosRecyclerView.adapter = photosAdapter
    }

    private fun observeViewModel() {
        viewModel.userPhotosState.observe(viewLifecycleOwner) { state ->
            progressBar.isVisible = state is UserPhotosUiState.Loading
            photosRecyclerView.isVisible = false
            noPhotosTextView.isVisible = false

            when (state) {
                is UserPhotosUiState.Success -> {
                    userEmailTextView.text = state.userEmail ?: "Email не найден"
                    if (state.photos.isNotEmpty()) {
                        photosRecyclerView.isVisible = true
                        photosAdapter.updateData(state.photos)
                    } else {
                        noPhotosTextView.isVisible = true
                        photosAdapter.updateData(emptyList())
                    }
                }
                is UserPhotosUiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    userEmailTextView.text = "Ошибка загрузки"
                    photosAdapter.updateData(emptyList())
                }
                is UserPhotosUiState.Loading -> {}
            }
        }
    }
}
