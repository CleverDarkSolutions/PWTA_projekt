package com.example.pwta_projekt.domain.models

data class Point2D(
    val x: Float,
    val y: Float
)

data class BoundingBox(
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
    val minZ: Float,
    val maxZ: Float
) {
    val centerX: Float get() = (minX + maxX) / 2f
    val centerY: Float get() = (minY + maxY) / 2f
    val centerZ: Float get() = (minZ + maxZ) / 2f

    val sizeX: Float get() = maxX - minX
    val sizeY: Float get() = maxY - minY
    val sizeZ: Float get() = maxZ - minZ

    val maxDimension: Float get() = maxOf(sizeX, sizeY, sizeZ)
}

data class Bounds2D(
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float
) {
    val centerX: Float get() = (minX + maxX) / 2f
    val centerY: Float get() = (minY + maxY) / 2f

    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY

    val maxDimension: Float get() = maxOf(width, height)
}
