package com.example.grnz.ui.main

import androidx.lifecycle.*
import com.example.grnz.data.network.ApiClient
import com.example.grnz.data.network.model.Photo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException


sealed class FeedUiState {
    data class Loading(val isRefresh: Boolean) : FeedUiState()
    data class Success(val photos: List<Photo>, val canLoadMore: Boolean) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
    object Empty : FeedUiState()
    object LoadingMore : FeedUiState()
}

class FeedViewModel : ViewModel() {

    companion object {
        private const val PAGE_LIMIT = 20
    }

    private val _uiState = MutableLiveData<FeedUiState>()
    val uiState: LiveData<FeedUiState> = _uiState

    private val currentPhotos = mutableListOf<Photo>()
    private var currentPage = 0
    private var totalPages = Int.MAX_VALUE
    internal var isLoading = false
    private var currentLoadingJob: Job? = null

    private var currentNumberFilter: String? = null
    private var currentLetterFilter: String? = null
    private var currentRegionFilter: String? = null

    fun loadInitialFeed() {
        if (isLoading && _uiState.value is FeedUiState.Loading && (_uiState.value as FeedUiState.Loading).isRefresh) return
        currentPage = 0
        totalPages = Int.MAX_VALUE
        currentNumberFilter = null
        currentLetterFilter = null
        currentRegionFilter = null
        fetchFeed(page = 1, isRefresh = true)
    }

    fun loadMoreFeed() {
        if (!isLoading && currentPage < totalPages) {
            fetchFeed(page = currentPage + 1, isRefresh = false)
        }
    }

    fun applyFilters(number: String?, letter: String?, region: String?) {
        if (isLoading) return
        currentNumberFilter = number?.takeIf { it.isNotBlank() }
        currentLetterFilter = letter?.takeIf { it.isNotBlank() }?.uppercase()
        currentRegionFilter = region?.takeIf { it.isNotBlank() }
        currentPage = 0
        totalPages = Int.MAX_VALUE
        fetchFeed(page = 1, isRefresh = true)
    }

    private fun fetchFeed(page: Int, isRefresh: Boolean) {
        if (isLoading) return
        isLoading = true
        currentLoadingJob?.cancel()

        if (isRefresh) {
            _uiState.value = FeedUiState.Loading(isRefresh = true)
        } else {
            _uiState.value = FeedUiState.LoadingMore
        }

        currentLoadingJob = viewModelScope.launch {
            try {
                val response = ApiClient.grnzService.getFeed(
                    page = page,
                    limit = PAGE_LIMIT,
                    numberPart = currentNumberFilter,
                    letterPart = currentLetterFilter,
                    regionCode = currentRegionFilter
                )
                if (response.isSuccessful && response.body() != null) {
                    val feedResponse = response.body()!!
                    currentPage = feedResponse.currentPage
                    totalPages = feedResponse.totalPages

                    if (isRefresh) {
                        currentPhotos.clear()
                    }
                    currentPhotos.addAll(feedResponse.photos)
                    val canLoadMore = currentPage < totalPages
                    if (currentPhotos.isEmpty()) {
                        _uiState.postValue(FeedUiState.Empty)
                    } else {
                        _uiState.postValue(FeedUiState.Success(currentPhotos.toList(), canLoadMore))
                    }

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown server error"
                    if (!isRefresh && _uiState.value is FeedUiState.Success) {
                        val previousState = _uiState.value as FeedUiState.Success
                        _uiState.postValue(previousState.copy(canLoadMore = false))
                    } else {
                        _uiState.postValue(FeedUiState.Error("Ошибка ${response.code()}: $errorBody"))
                    }
                    if (!isRefresh) totalPages = currentPage
                }
            } catch (e: HttpException) {
                if (!isRefresh && _uiState.value is FeedUiState.Success) {
                    val previousState = _uiState.value as FeedUiState.Success
                    _uiState.postValue(previousState.copy(canLoadMore = false))
                } else {
                    _uiState.postValue(FeedUiState.Error("Ошибка сети: ${e.message()}"))
                }
                if (!isRefresh) totalPages = currentPage
            } catch (e: IOException) {
                if (e !is CancellationException) {
                    if (!isRefresh && _uiState.value is FeedUiState.Success) {
                        val previousState = _uiState.value as FeedUiState.Success
                        _uiState.postValue(previousState.copy(canLoadMore = false))
                    } else {
                        _uiState.postValue(FeedUiState.Error("Ошибка ввода-вывода: ${e.message}"))
                    }
                    if (!isRefresh) totalPages = currentPage
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    if (!isRefresh && _uiState.value is FeedUiState.Success) {
                        val previousState = _uiState.value as FeedUiState.Success
                        _uiState.postValue(previousState.copy(canLoadMore = false))
                    } else {
                        _uiState.postValue(FeedUiState.Error("Неизвестная ошибка: ${e.message}"))
                    }
                    if (!isRefresh) totalPages = currentPage
                }
            } finally {
                isLoading = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentLoadingJob?.cancel()
    }
}
