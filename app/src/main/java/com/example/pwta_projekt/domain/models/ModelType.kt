package com.example.pwta_projekt.domain.models

enum class ModelType(val extension: String, val displayName: String) {
    STL(".stl", "STL (3D)"),
    DXF(".dxf", "DXF (2D)");

    companion object {
        fun fromFilename(filename: String): ModelType? {
            return when {
                filename.endsWith(".stl", ignoreCase = true) -> STL
                filename.endsWith(".dxf", ignoreCase = true) -> DXF
                else -> null
            }
        }
    }
}
