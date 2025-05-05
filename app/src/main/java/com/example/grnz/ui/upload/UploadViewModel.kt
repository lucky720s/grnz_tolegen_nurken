package com.example.grnz.ui.upload

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.*
import com.example.grnz.data.network.ApiClient
import com.example.grnz.data.network.model.Photo
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

sealed class UploadUiState {
    object Idle : UploadUiState()
    object Loading : UploadUiState()
    data class Success(val photo: Photo) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

class UploadViewModel : ViewModel() {

    private val _uploadState = MutableLiveData<UploadUiState>(UploadUiState.Idle)
    val uploadState: LiveData<UploadUiState> = _uploadState

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
        if (uri != null && _uploadState.value !is UploadUiState.Idle) {
            _uploadState.value = UploadUiState.Idle
        }
    }

    fun uploadPhoto(grnz: String, imageUri: Uri, contentResolver: ContentResolver, cacheDir: File) {
        if (grnz.isBlank()) {
            _uploadState.value = UploadUiState.Error("Введите ГРНЗ")
            return
        }
        _uploadState.value = UploadUiState.Loading

        viewModelScope.launch {
            try {
                val grnzRequestBody = grnz.toRequestBody("text/plain".toMediaTypeOrNull())
                val filePart = createMultipartBodyPart(imageUri, contentResolver, cacheDir, "photoFile")
                if (filePart == null) {
                    _uploadState.postValue(UploadUiState.Error("Не удалось обработать файл изображения"))
                    return@launch
                }

                val response = ApiClient.grnzService.uploadPhoto(grnzRequestBody, filePart)

                if (response.isSuccessful && response.body() != null) {
                    _uploadState.postValue(UploadUiState.Success(response.body()!!))
                } else {
                    _uploadState.postValue(UploadUiState.Error("Ошибка загрузки: ${response.code()} ${response.message()}"))
                }

            } catch (e: HttpException) {
                _uploadState.postValue(UploadUiState.Error("Ошибка сети: ${e.message()}"))
            } catch (e: IOException) {
                _uploadState.postValue(UploadUiState.Error("Ошибка ввода-вывода: ${e.message}"))
            } catch (e: Exception) {
                _uploadState.postValue(UploadUiState.Error("Неизвестная ошибка: ${e.message}"))
            }
        }
    }

    private suspend fun createMultipartBodyPart(
        uri: Uri,
        contentResolver: ContentResolver,
        cacheDir: File,
        partName: String
    ): MultipartBody.Part? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileType = contentResolver.getType(uri)
            val fileName = getFileName(uri, contentResolver) ?: "upload.tmp"
            val tempFile = File(cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            val requestFile = tempFile.asRequestBody(fileType?.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result?.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
    }
}
