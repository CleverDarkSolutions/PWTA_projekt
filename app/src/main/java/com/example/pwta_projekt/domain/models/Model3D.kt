package com.example.pwta_projekt.domain.models

data class Model3D(
    val vertices: FloatArray,
    val normals: FloatArray,
    val triangleCount: Int,
    val bounds: BoundingBox
) {
    val vertexCount: Int get() = triangleCount * 3

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Model3D

        if (!vertices.contentEquals(other.vertices)) return false
        if (!normals.contentEquals(other.normals)) return false
        if (triangleCount != other.triangleCount) return false
        if (bounds != other.bounds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vertices.contentHashCode()
        result = 31 * result + normals.contentHashCode()
        result = 31 * result + triangleCount
        result = 31 * result + bounds.hashCode()
        return result
    }
}
