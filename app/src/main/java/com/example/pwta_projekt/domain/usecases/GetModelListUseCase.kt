package com.example.pwta_projekt.domain.usecases

import com.example.pwta_projekt.data.repository.ModelRepository
import com.example.pwta_projekt.domain.models.ModelInfo

class GetModelListUseCase(private val repository: ModelRepository) {
    operator fun invoke(): List<ModelInfo> {
        return repository.getModelList()
    }
}
