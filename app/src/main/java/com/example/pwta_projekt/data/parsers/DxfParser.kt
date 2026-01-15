package com.example.pwta_projekt.data.parsers

import android.util.Log
import com.example.pwta_projekt.domain.models.Bounds2D
import com.example.pwta_projekt.domain.models.DxfEntity
import com.example.pwta_projekt.domain.models.Model2D
import com.example.pwta_projekt.domain.models.Point2D
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class DxfParser {
    fun parse(inputStream: InputStream): Model2D {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val entities = mutableListOf<DxfEntity>()
        val lines = reader.readLines()

        var inEntitiesSection = false
        var i = 0

        while (i < lines.size) {
            val code = lines[i].trim()
            i++
            if (i >= lines.size) break
            val value = lines[i].trim()
            i++

            if (code == "0" && value == "SECTION") {
                // Check next code/value pair for ENTITIES
                if (i < lines.size - 1 && lines[i].trim() == "2" && lines[i + 1].trim() == "ENTITIES") {
                    inEntitiesSection = true
                    i += 2
                    continue
                }
            }

            if (code == "0" && value == "ENDSEC") {
                inEntitiesSection = false
                continue
            }

            if (inEntitiesSection && code == "0") {
                try {
                    when (value) {
                        "LINE" -> {
                            Log.d("DxfParser", "Parsing LINE at line $i")
                            entities.add(parseLine(lines, i))
                        }
                        "ARC" -> {
                            Log.d("DxfParser", "Parsing ARC at line $i")
                            entities.add(parseArc(lines, i))
                        }
                        "CIRCLE" -> {
                            Log.d("DxfParser", "Parsing CIRCLE at line $i")
                            entities.add(parseCircle(lines, i))
                        }
                        "POLYLINE" -> {
                            Log.d("DxfParser", "Parsing POLYLINE at line $i")
                            entities.add(parsePolyline(lines, i))
                        }
                        "LWPOLYLINE" -> {
                            Log.d("DxfParser", "Parsing LWPOLYLINE at line $i")
                            entities.add(parseLwPolyline(lines, i))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DxfParser", "Error parsing entity $value at line $i: ${e.message}")
                }
            }
        }

        Log.d("DxfParser", "Parsed ${entities.size} entities")

        if (entities.isEmpty()) {
            Log.w("DxfParser", "No entities found in DXF file")
            // Return a default model with a simple line for visualization
            return Model2D(
                entities = listOf(
                    DxfEntity.Line(Point2D(0f, 0f), Point2D(100f, 100f))
                ),
                bounds = Bounds2D(0f, 100f, 0f, 100f)
            )
        }

        val bounds = calculateBounds(entities)
        return Model2D(entities = entities, bounds = bounds)
    }

    private fun parseLine(lines: List<String>, startIndex: Int): DxfEntity.Line {
        var x1 = 0f
        var y1 = 0f
        var x2 = 0f
        var y2 = 0f

        var i = startIndex
        while (i < lines.size - 1) {
            val code = lines[i].trim()
            val value = lines[i + 1].trim()

            when (code) {
                "0" -> break  // Next entity
                "10" -> x1 = value.toFloatOrNull() ?: 0f
                "20" -> y1 = value.toFloatOrNull() ?: 0f
                "11" -> x2 = value.toFloatOrNull() ?: 0f
                "21" -> y2 = value.toFloatOrNull() ?: 0f
            }
            i += 2
        }

        return DxfEntity.Line(Point2D(x1, y1), Point2D(x2, y2))
    }

    private fun parseArc(lines: List<String>, startIndex: Int): DxfEntity.Arc {
        var cx = 0f
        var cy = 0f
        var radius = 0f
        var startAngle = 0f
        var endAngle = 0f

        var i = startIndex
        while (i < lines.size - 1) {
            val code = lines[i].trim()
            val value = lines[i + 1].trim()

            when (code) {
                "0" -> break
                "10" -> cx = value.toFloatOrNull() ?: 0f
                "20" -> cy = value.toFloatOrNull() ?: 0f
                "40" -> radius = value.toFloatOrNull() ?: 0f
                "50" -> startAngle = value.toFloatOrNull() ?: 0f
                "51" -> endAngle = value.toFloatOrNull() ?: 0f
            }
            i += 2
        }

        // DXF stores angles in degrees, keep them as degrees (Canvas API expects degrees)
        return DxfEntity.Arc(Point2D(cx, cy), radius, startAngle, endAngle)
    }

    private fun parseCircle(lines: List<String>, startIndex: Int): DxfEntity.Circle {
        var cx = 0f
        var cy = 0f
        var radius = 0f

        var i = startIndex
        while (i < lines.size - 1) {
            val code = lines[i].trim()
            val value = lines[i + 1].trim()

            when (code) {
                "0" -> break
                "10" -> cx = value.toFloatOrNull() ?: 0f
                "20" -> cy = value.toFloatOrNull() ?: 0f
                "40" -> radius = value.toFloatOrNull() ?: 0f
            }
            i += 2
        }

        return DxfEntity.Circle(Point2D(cx, cy), radius)
    }

    private fun parsePolyline(lines: List<String>, startIndex: Int): DxfEntity.Polyline {
        val points = mutableListOf<Point2D>()
        var closed = false
        var i = startIndex

        // Check if polyline is closed
        while (i < lines.size - 1) {
            val code = lines[i].trim()
            val value = lines[i + 1].trim()

            if (code == "0") {
                if (value == "VERTEX") {
                    // Parse vertex
                    var x = 0f
                    var y = 0f
                    var j = i + 2

                    while (j < lines.size - 1) {
                        val vCode = lines[j].trim()
                        val vValue = lines[j + 1].trim()

                        when (vCode) {
                            "0" -> break
                            "10" -> x = vValue.toFloatOrNull() ?: 0f
                            "20" -> y = vValue.toFloatOrNull() ?: 0f
                        }
                        j += 2
                    }

                    points.add(Point2D(x, y))
                    i = j
                } else if (value == "SEQEND") {
                    break
                } else {
                    i += 2
                }
            } else if (code == "70") {
                closed = (value.toIntOrNull() ?: 0) and 1 != 0
                i += 2
            } else {
                i += 2
            }
        }

        return DxfEntity.Polyline(points, closed)
    }

    private fun parseLwPolyline(lines: List<String>, startIndex: Int): DxfEntity.LwPolyline {
        val points = mutableListOf<Point2D>()
        var closed = false
        var numVertices = 0

        val xValues = mutableListOf<Float>()
        val yValues = mutableListOf<Float>()

        var i = startIndex
        while (i < lines.size - 1) {
            val code = lines[i].trim()
            val value = lines[i + 1].trim()

            when (code) {
                "0" -> break
                "90" -> numVertices = value.toIntOrNull() ?: 0
                "70" -> closed = (value.toIntOrNull() ?: 0) and 1 != 0
                "10" -> xValues.add(value.toFloatOrNull() ?: 0f)
                "20" -> yValues.add(value.toFloatOrNull() ?: 0f)
            }
            i += 2
        }

        // Combine x and y values into points
        val minSize = minOf(xValues.size, yValues.size)
        for (j in 0 until minSize) {
            points.add(Point2D(xValues[j], yValues[j]))
        }

        return DxfEntity.LwPolyline(points, closed)
    }

    private fun calculateBounds(entities: List<DxfEntity>): Bounds2D {
        if (entities.isEmpty()) {
            return Bounds2D(0f, 0f, 0f, 0f)
        }

        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        fun updateBounds(point: Point2D) {
            minX = minOf(minX, point.x)
            maxX = maxOf(maxX, point.x)
            minY = minOf(minY, point.y)
            maxY = maxOf(maxY, point.y)
        }

        entities.forEach { entity ->
            when (entity) {
                is DxfEntity.Line -> {
                    updateBounds(entity.start)
                    updateBounds(entity.end)
                }
                is DxfEntity.Arc -> {
                    // Approximate bounds with center +/- radius
                    updateBounds(Point2D(entity.center.x - entity.radius, entity.center.y - entity.radius))
                    updateBounds(Point2D(entity.center.x + entity.radius, entity.center.y + entity.radius))
                }
                is DxfEntity.Circle -> {
                    updateBounds(Point2D(entity.center.x - entity.radius, entity.center.y - entity.radius))
                    updateBounds(Point2D(entity.center.x + entity.radius, entity.center.y + entity.radius))
                }
                is DxfEntity.Polyline -> {
                    entity.points.forEach { updateBounds(it) }
                }
                is DxfEntity.LwPolyline -> {
                    entity.points.forEach { updateBounds(it) }
                }
            }
        }

        // Validate bounds
        if (minX == Float.MAX_VALUE || maxX == Float.MIN_VALUE) {
            Log.w("DxfParser", "Invalid bounds after calculation, using defaults")
            return Bounds2D(0f, 100f, 0f, 100f)
        }

        Log.d("DxfParser", "Bounds: ($minX, $minY) to ($maxX, $maxY)")
        return Bounds2D(minX, maxX, minY, maxY)
    }
}
