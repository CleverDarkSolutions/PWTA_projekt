package com.example.pwta_projekt.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pwta_projekt.domain.models.Model2D
import com.example.pwta_projekt.domain.models.Model3D
import com.example.pwta_projekt.domain.models.ModelInfo
import com.example.pwta_projekt.domain.models.ModelType
import com.example.pwta_projekt.domain.usecases.GetModelListUseCase
import com.example.pwta_projekt.domain.usecases.LoadModelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ModelViewerUiState {
    object Loading : ModelViewerUiState()
    data class Error(val message: String) : ModelViewerUiState()
    data class Success3D(
        val model3D: Model3D,
        val modelInfo: ModelInfo
    ) : ModelViewerUiState()
    data class Success2D(
        val model2D: Model2D,
        val modelInfo: ModelInfo
    ) : ModelViewerUiState()
}

class ModelViewerViewModel(
    private val getModelListUseCase: GetModelListUseCase,
    private val loadModelUseCase: LoadModelUseCase,
    private val modelPath: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ModelViewerUiState>(ModelViewerUiState.Loading)
    val uiState: StateFlow<ModelViewerUiState> = _uiState.asStateFlow()

    init {
        loadModel()
    }

    private fun loadModel() {
        viewModelScope.launch {
            try {
                _uiState.value = ModelViewerUiState.Loading

                // Find model info
                val models = getModelListUseCase()
                val modelInfo = models.firstOrNull { it.path == modelPath }
                    ?: throw IllegalArgumentException("Model not found: $modelPath")

                // Load model based on type
                when (modelInfo.type) {
                    ModelType.STL -> {
                        val model3D = loadModelUseCase.load3DModel(modelInfo)
                        _uiState.value = ModelViewerUiState.Success3D(model3D, modelInfo)
                    }
                    ModelType.DXF -> {
                        val model2D = loadModelUseCase.load2DModel(modelInfo)
                        _uiState.value = ModelViewerUiState.Success2D(model2D, modelInfo)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ModelViewerUiState.Error(
                    e.message ?: "Failed to load model"
                )
            }
        }
    }
}
