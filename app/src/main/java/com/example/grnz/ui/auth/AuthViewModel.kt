package com.example.grnz.ui.auth

import android.app.Application
import androidx.lifecycle.*
import com.example.grnz.data.network.ApiClient
import com.example.grnz.data.network.model.LoginRequest
import com.example.grnz.data.network.model.LoginResponse
import com.example.grnz.data.network.model.RegisterRequest
import com.example.grnz.util.TokenManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: LoginResponse? = null, val message: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private val tokenManager = TokenManager(application)

    fun registerUser(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.authService.register(RegisterRequest(email, password))
                if (response.isSuccessful) {
                    _authState.postValue(AuthState.Success(message = "Регистрация успешна!"))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Ошибка регистрации (${response.code()})"
                    _authState.postValue(AuthState.Error(errorMsg))
                }
            } catch (e: HttpException) {
                _authState.postValue(AuthState.Error("Ошибка сети: ${e.message()}"))
            } catch (e: IOException) {
                _authState.postValue(AuthState.Error("Ошибка соединения: ${e.message}"))
            } catch (e: Exception) {
                _authState.postValue(AuthState.Error("Неизвестная ошибка: ${e.message}"))
            }
        }
    }

    fun loginUser(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.authService.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    tokenManager.saveToken(loginResponse.accessToken)
                    _authState.postValue(AuthState.Success(response = loginResponse, message = "Вход успешен!"))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Неверные учетные данные (${response.code()})"
                    _authState.postValue(AuthState.Error(errorMsg))
                }
            } catch (e: HttpException) {
                _authState.postValue(AuthState.Error("Ошибка сети: ${e.message()}"))
            } catch (e: IOException) {
                _authState.postValue(AuthState.Error("Ошибка соединения: ${e.message}"))
            } catch (e: Exception) {
                _authState.postValue(AuthState.Error("Неизвестная ошибка: ${e.message}"))
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
