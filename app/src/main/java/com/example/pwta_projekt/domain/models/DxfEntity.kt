package com.example.pwta_projekt.domain.models

sealed class DxfEntity {
    data class Line(
        val start: Point2D,
        val end: Point2D
    ) : DxfEntity()

    data class Arc(
        val center: Point2D,
        val radius: Float,
        val startAngle: Float,
        val endAngle: Float
    ) : DxfEntity()

    data class Polyline(
        val points: List<Point2D>,
        val closed: Boolean
    ) : DxfEntity()

    data class Circle(
        val center: Point2D,
        val radius: Float
    ) : DxfEntity()

    data class LwPolyline(
        val points: List<Point2D>,
        val closed: Boolean
    ) : DxfEntity()
}
