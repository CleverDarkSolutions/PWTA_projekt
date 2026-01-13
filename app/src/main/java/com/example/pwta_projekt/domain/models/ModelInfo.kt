package com.example.pwta_projekt.domain.models

data class ModelInfo(
    val filename: String,
    val path: String,
    val type: ModelType,
    val fileSizeBytes: Long
) {
    val fileSizeKB: Float get() = fileSizeBytes / 1024f
    val fileSizeMB: Float get() = fileSizeKB / 1024f

    val formattedFileSize: String
        get() = when {
            fileSizeMB >= 1f -> String.format("%.2f MB", fileSizeMB)
            else -> String.format("%.2f KB", fileSizeKB)
        }
}
