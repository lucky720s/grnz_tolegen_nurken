package com.example.grnz.ui.main

import androidx.lifecycle.*
import com.example.grnz.data.network.ApiClient
import com.example.grnz.data.network.model.Photo
import com.example.grnz.data.network.model.VehicleData
import com.example.grnz.util.Event
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val vehicle: VehicleData?, val photos: List<Photo>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class SearchViewModel : ViewModel() {

    private val _searchState = MutableLiveData<SearchUiState>(SearchUiState.Idle)
    val searchState: LiveData<SearchUiState> = _searchState

    private val _searchHistory = MutableLiveData<List<String>>()
    val searchHistory: LiveData<List<String>> = _searchHistory
    private val _toastEvent = MutableLiveData<Event<String>>()
    val toastEvent: LiveData<Event<String>> = _toastEvent
    init {
        loadSearchHistory()
    }

    fun searchNumber(grnz: String) {
        _searchState.value = SearchUiState.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.grnzService.searchPhotos(grnz)
                if (response.isSuccessful && response.body() != null) {
                    val searchResponse = response.body()!!
                    _searchState.postValue(SearchUiState.Success(
                        vehicle = searchResponse.vehicle,
                        photos = searchResponse.photos
                    ))
                    loadSearchHistoryInternal()
                } else {
                    _searchState.postValue(SearchUiState.Error("Ошибка: ${response.code()}"))
                }
            } catch (e: HttpException) {
                _searchState.postValue(SearchUiState.Error("Ошибка сети: ${e.message()}"))
            } catch (e: IOException) {
                _searchState.postValue(SearchUiState.Error("Ошибка соединения"))
            } catch (e: Exception) {
                _searchState.postValue(SearchUiState.Error("Неизвестная ошибка: ${e.message}"))
            }
        }
    }
    fun loadSearchHistory() {
        viewModelScope.launch {
            loadSearchHistoryInternal()
        }
    }
    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                val response = ApiClient.grnzService.clearSearchHistory()
                if (response.isSuccessful) {
                    // Успешно очищено на бэкенде, обновляем LiveData
                    _searchHistory.postValue(emptyList())
                    _toastEvent.postValue(Event("История поиска очищена"))
                } else {
                    // Ошибка на бэкенде
                    val errorMsg = "Ошибка очистки истории: ${response.code()}"
                    _toastEvent.postValue(Event(errorMsg))
                }
            } catch (e: HttpException) {
                val errorMsg = "Ошибка сети при очистке истории: ${e.message()}"
                _toastEvent.postValue(Event(errorMsg))
            } catch (e: IOException) {
                val errorMsg = "Ошибка соединения при очистке истории"
                _toastEvent.postValue(Event(errorMsg))
            } catch (e: Exception) {
                val errorMsg = "Неизвестная ошибка при очистке истории: ${e.message}"
                _toastEvent.postValue(Event(errorMsg))
            }
        }
    }
    private suspend fun loadSearchHistoryInternal() {
        try {
            val historyResponse = ApiClient.grnzService.getSearchHistory()
            if (historyResponse.isSuccessful) {
                _searchHistory.postValue(historyResponse.body() ?: emptyList())
            } else {
                println("Error loading search history: ${historyResponse.code()}")
            }
        } catch (e: Exception) {
            println("Exception loading search history: ${e.message}")
        }
    }
}
