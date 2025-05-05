package com.example.grnz.ui.userphotos

import androidx.lifecycle.*
import com.example.grnz.data.network.ApiClient
import com.example.grnz.data.network.model.Photo
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class UserPhotosUiState {
    object Loading : UserPhotosUiState()
    data class Success(val userEmail: String?, val photos: List<Photo>) : UserPhotosUiState()
    data class Error(val message: String) : UserPhotosUiState()
}

class UserPhotosViewModel : ViewModel() {

    private val _userPhotosState = MutableLiveData<UserPhotosUiState>()
    val userPhotosState: LiveData<UserPhotosUiState> = _userPhotosState

    fun loadUserPhotos(userId: String) {
        _userPhotosState.value = UserPhotosUiState.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.grnzService.getUserPhotos(userId)

                if (response.isSuccessful && response.body() != null) {
                    val responseData = response.body()!!
                    _userPhotosState.postValue(UserPhotosUiState.Success(responseData.userEmail, responseData.photos))
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown server error"
                    _userPhotosState.postValue(UserPhotosUiState.Error("Ошибка ${response.code()}: $errorBody"))
                }
            } catch (e: HttpException) {
                _userPhotosState.postValue(UserPhotosUiState.Error("Ошибка сети: ${e.message()}"))
            } catch (e: IOException) {
                _userPhotosState.postValue(UserPhotosUiState.Error("Ошибка ввода-вывода: ${e.message}"))
            } catch (e: Exception) {
                _userPhotosState.postValue(UserPhotosUiState.Error("Неизвестная ошибка: ${e.message}"))
            }
        }
    }
}
