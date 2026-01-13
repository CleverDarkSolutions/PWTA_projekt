package com.example.pwta_projekt.data.repository

import android.content.Context
import com.example.pwta_projekt.domain.models.ModelInfo
import com.example.pwta_projekt.domain.models.ModelType
import java.io.IOException
import java.io.InputStream

class ModelRepository(private val context: Context) {
    private val modelsPath = "models"

    fun getModelList(): List<ModelInfo> {
        val modelInfoList = mutableListOf<ModelInfo>()

        try {
            // List 3D models
            val stlFiles = context.assets.list("$modelsPath/3d") ?: emptyArray()
            stlFiles.filter { it.endsWith(".stl", ignoreCase = true) }.forEach { filename ->
                val path = "$modelsPath/3d/$filename"
                val size = getFileSize(path)
                modelInfoList.add(
                    ModelInfo(
                        filename = filename,
                        path = path,
                        type = ModelType.STL,
                        fileSizeBytes = size
                    )
                )
            }

            // List 2D models
            val dxfFiles = context.assets.list("$modelsPath/2d") ?: emptyArray()
            dxfFiles.filter { it.endsWith(".dxf", ignoreCase = true) }.forEach { filename ->
                val path = "$modelsPath/2d/$filename"
                val size = getFileSize(path)
                modelInfoList.add(
                    ModelInfo(
                        filename = filename,
                        path = path,
                        type = ModelType.DXF,
                        fileSizeBytes = size
                    )
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return modelInfoList.sortedBy { it.type }
    }

    fun openModelStream(path: String): InputStream {
        return context.assets.open(path)
    }

    private fun getFileSize(path: String): Long {
        return try {
            val fd = context.assets.openFd(path)
            val size = fd.length
            fd.close()
            size
        } catch (e: Exception) {
            0L
        }
    }
}
