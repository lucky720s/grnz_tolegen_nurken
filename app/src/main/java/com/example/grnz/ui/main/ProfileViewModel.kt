package com.example.grnz.ui.main
import androidx.lifecycle.*
import com.example.grnz.data.network.ApiClient
import com.example.grnz.data.network.model.Photo
import com.example.grnz.data.network.model.ProfileResponse
import com.example.grnz.data.network.model.UserPhotosResponse
import com.example.grnz.util.Event
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ProfileData(
    val email: String?,
    val userId: String?,
    val photos: List<Photo>
)

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val data: ProfileData) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {

    private val _profileState = MutableLiveData<ProfileUiState>()
    val profileState: LiveData<ProfileUiState> = _profileState

    private val _toastEvent = MutableLiveData<Event<String>>()
    val toastEvent: LiveData<Event<String>> = _toastEvent

    fun loadProfileData() {
        _profileState.value = ProfileUiState.Loading
        viewModelScope.launch {
            try {
                val profileInfoResponse = ApiClient.authService.getProfile()

                if (!profileInfoResponse.isSuccessful) {
                    val errorBody = profileInfoResponse.errorBody()?.string() ?: "Unknown server error getting profile info"
                    val errorMsg = "Ошибка ${profileInfoResponse.code()}: $errorBody"
                    _profileState.postValue(ProfileUiState.Error(errorMsg))
                    _toastEvent.postValue(Event(errorMsg))
                    return@launch
                }
                profileInfoResponse.body()?.let { profileInfo ->
                    val userId = profileInfo.id
                    val userEmail = profileInfo.email
                    viewModelScope.launch {
                        try {
                            val userPhotosResponse = ApiClient.grnzService.getUserPhotos(userId)

                            if (!userPhotosResponse.isSuccessful) {
                                val errorBody = userPhotosResponse.errorBody()?.string() ?: "Unknown server error getting user photos"
                                val errorMsg = "Не удалось загрузить фото: ${userPhotosResponse.code()}"
                                _profileState.postValue(ProfileUiState.Success(ProfileData(userEmail, userId, emptyList())))
                                _toastEvent.postValue(Event(errorMsg))
                                return@launch
                            }
                            val photos = userPhotosResponse.body()?.photos ?: emptyList()
                            val finalProfileData = ProfileData(
                                email = userEmail,
                                userId = userId,
                                photos = photos
                            )
                            _profileState.postValue(ProfileUiState.Success(finalProfileData))

                        } catch (e: HttpException) {
                            val errorMsg = "Ошибка сети при загрузке фото: ${e.message()}"
                            _profileState.postValue(ProfileUiState.Error(errorMsg))
                            _toastEvent.postValue(Event(errorMsg))
                        } catch (e: IOException) {
                            val errorMsg = "Ошибка ввода-вывода при загрузке фото"
                            _profileState.postValue(ProfileUiState.Error(errorMsg))
                            _toastEvent.postValue(Event(errorMsg))
                        } catch (e: Exception) {
                            val errorMsg = "Неизвестная ошибка при загрузке фото"
                            _profileState.postValue(ProfileUiState.Error(errorMsg))
                            _toastEvent.postValue(Event(errorMsg))
                        }
                    }

                } ?: run {
                    val errorMsg = "Ошибка: Пустой ответ от сервера при запросе профиля"
                    _profileState.postValue(ProfileUiState.Error(errorMsg))
                    _toastEvent.postValue(Event(errorMsg))
                    return@launch
                }

            } catch (e: HttpException) {
                val errorMsg = "Ошибка сети при запросе профиля: ${e.message()}"
                _profileState.postValue(ProfileUiState.Error(errorMsg))
                _toastEvent.postValue(Event(errorMsg))
            } catch (e: IOException) {
                val errorMsg = "Ошибка ввода-вывода при запросе профиля"
                _profileState.postValue(ProfileUiState.Error(errorMsg))
                _toastEvent.postValue(Event(errorMsg))
            } catch (e: Exception) {
                val errorMsg = "Неизвестная ошибка при запросе профиля"
                _profileState.postValue(ProfileUiState.Error(errorMsg))
                _toastEvent.postValue(Event(errorMsg))
            }
        }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            val previousState = _profileState.value
            try {
                val response = ApiClient.grnzService.deletePhoto(photoId)

                if (response.isSuccessful) {
                    _toastEvent.postValue(Event("Фото успешно удалено"))
                    loadProfileData()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown server error"
                    val errorMsg = "Ошибка удаления ${response.code()}: $errorBody"
                    previousState?.let { _profileState.postValue(it) }
                    _toastEvent.postValue(Event(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = "Ошибка при удалении: ${e.message}"
                previousState?.let { _profileState.postValue(it) }
                _toastEvent.postValue(Event(errorMsg))
            }
        }
    }
}
