package com.example.pwta_projekt.data.parsers

import com.example.pwta_projekt.domain.models.BoundingBox
import com.example.pwta_projekt.domain.models.Model3D
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

class StlParser {
    private enum class StlFormat {
        ASCII, BINARY
    }

    fun parse(inputStream: InputStream): Model3D {
        val bufferedStream = inputStream.buffered()
        bufferedStream.mark(6)

        val format = detectFormat(bufferedStream)
        bufferedStream.reset()

        return when (format) {
            StlFormat.ASCII -> parseAscii(bufferedStream)
            StlFormat.BINARY -> parseBinary(bufferedStream)
        }
    }

    private fun detectFormat(stream: InputStream): StlFormat {
        val header = ByteArray(5)
        stream.read(header)
        val headerStr = String(header, Charsets.US_ASCII)

        return if (headerStr.equals("solid", ignoreCase = true)) {
            StlFormat.ASCII
        } else {
            StlFormat.BINARY
        }
    }

    private fun parseBinary(stream: InputStream): Model3D {
        val allBytes = stream.readBytes()
        val buffer = ByteBuffer.wrap(allBytes).order(ByteOrder.LITTLE_ENDIAN)

        // Skip 80-byte header
        buffer.position(80)

        // Read triangle count
        val triangleCount = buffer.int

        val vertices = mutableListOf<Float>()
        val normals = mutableListOf<Float>()

        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        var minZ = Float.MAX_VALUE
        var maxZ = Float.MIN_VALUE

        repeat(triangleCount) {
            // Read normal (3 floats)
            val nx = buffer.float
            val ny = buffer.float
            val nz = buffer.float

            // Read 3 vertices (9 floats)
            repeat(3) {
                val x = buffer.float
                val y = buffer.float
                val z = buffer.float

                vertices.add(x)
                vertices.add(y)
                vertices.add(z)

                // Add same normal for each vertex
                normals.add(nx)
                normals.add(ny)
                normals.add(nz)

                // Update bounds
                minX = minOf(minX, x)
                maxX = maxOf(maxX, x)
                minY = minOf(minY, y)
                maxY = maxOf(maxY, y)
                minZ = minOf(minZ, z)
                maxZ = maxOf(maxZ, z)
            }

            // Skip attribute byte count (2 bytes)
            buffer.getShort()
        }

        val bounds = BoundingBox(minX, maxX, minY, maxY, minZ, maxZ)

        return Model3D(
            vertices = vertices.toFloatArray(),
            normals = normals.toFloatArray(),
            triangleCount = triangleCount,
            bounds = bounds
        )
    }

    private fun parseAscii(stream: InputStream): Model3D {
        val reader = BufferedReader(InputStreamReader(stream))
        val vertices = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        var triangleCount = 0

        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        var minZ = Float.MAX_VALUE
        var maxZ = Float.MIN_VALUE

        var currentNormal: Triple<Float, Float, Float>? = null

        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()

                when {
                    trimmed.startsWith("facet normal") -> {
                        val parts = trimmed.split("\\s+".toRegex())
                        if (parts.size >= 5) {
                            val nx = parts[2].toFloatOrNull() ?: 0f
                            val ny = parts[3].toFloatOrNull() ?: 0f
                            val nz = parts[4].toFloatOrNull() ?: 0f
                            currentNormal = Triple(nx, ny, nz)
                        }
                    }

                    trimmed.startsWith("vertex") -> {
                        val parts = trimmed.split("\\s+".toRegex())
                        if (parts.size >= 4) {
                            val x = parts[1].toFloatOrNull() ?: 0f
                            val y = parts[2].toFloatOrNull() ?: 0f
                            val z = parts[3].toFloatOrNull() ?: 0f

                            vertices.add(x)
                            vertices.add(y)
                            vertices.add(z)

                            currentNormal?.let { (nx, ny, nz) ->
                                normals.add(nx)
                                normals.add(ny)
                                normals.add(nz)
                            }

                            // Update bounds
                            minX = minOf(minX, x)
                            maxX = maxOf(maxX, x)
                            minY = minOf(minY, y)
                            maxY = maxOf(maxY, y)
                            minZ = minOf(minZ, z)
                            maxZ = maxOf(maxZ, z)
                        }
                    }

                    trimmed == "endfacet" -> {
                        triangleCount++
                        currentNormal = null
                    }
                }
            }
        }

        val bounds = BoundingBox(minX, maxX, minY, maxY, minZ, maxZ)

        return Model3D(
            vertices = vertices.toFloatArray(),
            normals = normals.toFloatArray(),
            triangleCount = triangleCount,
            bounds = bounds
        )
    }
}
