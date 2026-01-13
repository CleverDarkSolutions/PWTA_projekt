package com.example.pwta_projekt.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pwta_projekt.domain.models.ModelInfo
import com.example.pwta_projekt.domain.usecases.GetModelListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModelListUiState(
    val models: List<ModelInfo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ModelListViewModel(
    private val getModelListUseCase: GetModelListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelListUiState())
    val uiState: StateFlow<ModelListUiState> = _uiState.asStateFlow()

    init {
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val models = getModelListUseCase()
                _uiState.value = _uiState.value.copy(
                    models = models,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}
