package com.example.grnz.ui.upload

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.grnz.R

class UploadFragment : Fragment() {

    private lateinit var imageViewPreview: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var editTextGrnz: EditText
    private lateinit var buttonUpload: Button
    private lateinit var progressBar: ProgressBar

    private val viewModel: UploadViewModel by viewModels()

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setSelectedImageUri(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)
        imageViewPreview = view.findViewById(R.id.image_view_preview)
        buttonSelectImage = view.findViewById(R.id.button_select_image)
        editTextGrnz = view.findViewById(R.id.edit_text_upload_grnz)
        buttonUpload = view.findViewById(R.id.button_upload)
        progressBar = view.findViewById(R.id.progress_bar_upload)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        buttonSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        buttonUpload.setOnClickListener {
            val grnz = editTextGrnz.text.toString().trim()
            val imageUri = viewModel.selectedImageUri.value

            if (grnz.isBlank()) {
                Toast.makeText(context, "Введите ГРНЗ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (imageUri == null) {
                Toast.makeText(context, "Выберите фото", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.uploadPhoto(
                grnz,
                imageUri,
                requireContext().contentResolver,
                requireContext().cacheDir
            )
        }
    }

    private fun observeViewModel() {
        viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                Glide.with(this)
                    .load(uri)
                    .into(imageViewPreview)
            } else {
                imageViewPreview.setImageResource(android.R.color.darker_gray)
            }
        }
        viewModel.uploadState.observe(viewLifecycleOwner) { state ->
            progressBar.isVisible = state is UploadUiState.Loading
            buttonSelectImage.isEnabled = state !is UploadUiState.Loading
            buttonUpload.isEnabled = state !is UploadUiState.Loading
            editTextGrnz.isEnabled = state !is UploadUiState.Loading

            when (state) {
                is UploadUiState.Success -> {
                    Toast.makeText(context, "Фото успешно загружено!", Toast.LENGTH_LONG).show()
                    viewModel.setSelectedImageUri(null)
                    editTextGrnz.text.clear()
                    findNavController().popBackStack()
                }
                is UploadUiState.Error -> {
                    Toast.makeText(context, "Ошибка: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is UploadUiState.Loading -> {}
                is UploadUiState.Idle -> {}
            }
        }
    }
}
