package com.example.grnz.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grnz.R
import com.example.grnz.util.TokenManager

class ProfileFragment : Fragment() {

    private lateinit var tokenManager: TokenManager
    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var contentScrollView: NestedScrollView
    private lateinit var emailTextView: TextView
    private lateinit var userIdTextView: TextView
    private lateinit var noPhotosTextView: TextView
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var logoutButton: Button

    private lateinit var photosAdapter: ProfilePhotosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        progressBar = view.findViewById(R.id.progress_bar_profile)
        errorTextView = view.findViewById(R.id.text_view_profile_error)
        contentScrollView = view.findViewById(R.id.scroll_view_profile_content)
        emailTextView = view.findViewById(R.id.text_view_user_email_profile)
        userIdTextView = view.findViewById(R.id.text_view_user_id_profile)
        noPhotosTextView = view.findViewById(R.id.text_view_no_my_photos)
        photosRecyclerView = view.findViewById(R.id.recycler_view_my_photos)
        logoutButton = view.findViewById(R.id.button_logout)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())

        setupRecyclerView()
        setupLogoutButton()
        observeViewModel()
        viewModel.loadProfileData()
    }

    private fun setupRecyclerView() {
        photosAdapter = ProfilePhotosAdapter(emptyList()) { photoId ->
            showDeleteConfirmationDialog(photoId)
        }
        photosRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        photosRecyclerView.adapter = photosAdapter
    }

    private fun setupLogoutButton() {
        logoutButton.setOnClickListener {
            tokenManager.clearToken()
            findNavController().navigate(R.id.action_global_logout)
        }
    }

    private fun observeViewModel() {
        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            progressBar.isVisible = state is ProfileUiState.Loading
            errorTextView.isVisible = state is ProfileUiState.Error
            contentScrollView.isVisible = state is ProfileUiState.Success

            when (state) {
                is ProfileUiState.Loading -> {
                }
                is ProfileUiState.Success -> {
                    emailTextView.text = state.data.email ?: "Не указан"
                    userIdTextView.text = state.data.userId ?: "Неизвестен"

                    if (state.data.photos.isEmpty()) {
                        noPhotosTextView.isVisible = true
                        photosRecyclerView.isVisible = false
                        photosAdapter.updateData(emptyList())
                    } else {
                        noPhotosTextView.isVisible = false
                        photosRecyclerView.isVisible = true
                        photosAdapter.updateData(state.data.photos)
                    }
                }
                is ProfileUiState.Error -> {
                    errorTextView.text = state.message
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(photoId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение")
            .setMessage("Вы уверены, что хотите удалить это фото?")
            .setPositiveButton("Удалить") { dialog, _ ->
                viewModel.deletePhoto(photoId)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
