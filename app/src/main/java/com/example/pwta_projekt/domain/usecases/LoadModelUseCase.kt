package com.example.pwta_projekt.domain.usecases

import com.example.pwta_projekt.data.parsers.DxfParser
import com.example.pwta_projekt.data.parsers.StlParser
import com.example.pwta_projekt.data.repository.ModelRepository
import com.example.pwta_projekt.domain.models.Model2D
import com.example.pwta_projekt.domain.models.Model3D
import com.example.pwta_projekt.domain.models.ModelInfo
import com.example.pwta_projekt.domain.models.ModelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadModelUseCase(
    private val repository: ModelRepository,
    private val stlParser: StlParser,
    private val dxfParser: DxfParser
) {
    suspend fun load3DModel(modelInfo: ModelInfo): Model3D = withContext(Dispatchers.IO) {
        require(modelInfo.type == ModelType.STL) { "Model must be STL type" }
        val stream = repository.openModelStream(modelInfo.path)
        stlParser.parse(stream)
    }

    suspend fun load2DModel(modelInfo: ModelInfo): Model2D = withContext(Dispatchers.IO) {
        require(modelInfo.type == ModelType.DXF) { "Model must be DXF type" }
        val stream = repository.openModelStream(modelInfo.path)
        dxfParser.parse(stream)
    }
}
