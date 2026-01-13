package com.example.pwta_projekt.domain.models

data class Model2D(
    val entities: List<DxfEntity>,
    val bounds: Bounds2D
) {
    val entityCount: Int get() = entities.size
}
