package com.example.pwta_projekt.ui.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.pwta_projekt.domain.models.DxfEntity
import com.example.pwta_projekt.domain.models.Model2D
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Dxf2DViewer(
    model2D: Model2D,
    onResetRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(model2D) {
        Log.d("Dxf2DViewer", "Model loaded with ${model2D.entityCount} entities")
        Log.d("Dxf2DViewer", "Bounds: (${model2D.bounds.minX}, ${model2D.bounds.minY}) to (${model2D.bounds.maxX}, ${model2D.bounds.maxY})")
        Log.d("Dxf2DViewer", "Width: ${model2D.bounds.width}, Height: ${model2D.bounds.height}")
        model2D.entities.forEachIndexed { index, entity ->
            Log.d("Dxf2DViewer", "Entity $index: ${entity::class.simpleName}")
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(0f) }

    val resetTransform: () -> Unit = {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        rotation = 0f
        onResetRequested()
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotationChange ->
                    scale = (scale * zoom).coerceIn(0.1f, 10f)
                    offsetX += pan.x
                    offsetY += pan.y
                    rotation += rotationChange
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        Log.d("Dxf2DViewer", "=== CANVAS RENDER START ===")
        Log.d("Dxf2DViewer", "Canvas size: ${canvasWidth}x${canvasHeight}")

        // Draw white background first
        drawRect(color = Color.White, size = size)

        // Draw DXF model with simple scaling - no rotation, no complex transforms
        val modelWidth = model2D.bounds.width
        val modelHeight = model2D.bounds.height

        // Scale to fit 80% of canvas
        val scaleX = if (modelWidth > 0) canvasWidth * 0.8f / modelWidth else 1f
        val scaleY = if (modelHeight > 0) canvasHeight * 0.8f / modelHeight else 1f
        val fitScale = minOf(scaleX, scaleY)

        Log.d("Dxf2DViewer", "Model: ${modelWidth}x${modelHeight}, Scale: $fitScale")

        // Draw DXF entities with simple transform: scale and center
        model2D.entities.forEachIndexed { index, entity ->
            when (entity) {
                is DxfEntity.Line -> {
                    val x1 = (entity.start.x - model2D.bounds.minX) * fitScale + (canvasWidth - modelWidth * fitScale) / 2f
                    val y1 = (entity.start.y - model2D.bounds.minY) * fitScale + (canvasHeight - modelHeight * fitScale) / 2f
                    val x2 = (entity.end.x - model2D.bounds.minX) * fitScale + (canvasWidth - modelWidth * fitScale) / 2f
                    val y2 = (entity.end.y - model2D.bounds.minY) * fitScale + (canvasHeight - modelHeight * fitScale) / 2f

                    drawLine(
                        color = Color.Black,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2f
                    )
                }

                is DxfEntity.Circle -> {
                    val cx = (entity.center.x - model2D.bounds.minX) * fitScale + (canvasWidth - modelWidth * fitScale) / 2f
                    val cy = (entity.center.y - model2D.bounds.minY) * fitScale + (canvasHeight - modelHeight * fitScale) / 2f
                    val r = entity.radius * fitScale

                    drawCircle(
                        color = Color.Black,
                        center = Offset(cx, cy),
                        radius = r,
                        style = Stroke(width = 2f)
                    )
                }

                is DxfEntity.Arc -> {
                    val cx = (entity.center.x - model2D.bounds.minX) * fitScale + (canvasWidth - modelWidth * fitScale) / 2f
                    val cy = (entity.center.y - model2D.bounds.minY) * fitScale + (canvasHeight - modelHeight * fitScale) / 2f
                    val r = entity.radius * fitScale

                    val rect = Rect(
                        left = cx - r,
                        top = cy - r,
                        right = cx + r,
                        bottom = cy + r
                    )

                    var sweepAngle = entity.endAngle - entity.startAngle
                    if (sweepAngle < 0) sweepAngle += 360f

                    drawArc(
                        color = Color.Black,
                        startAngle = entity.startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = Stroke(width = 2f)
                    )
                }

                is DxfEntity.Polyline -> {
                    if (entity.points.isNotEmpty()) {
                        val path = Path()
                        val first = entity.points.first()
                        val fx = (first.x - model2D.bounds.minX) * fitScale + (canvasWidth - modelWidth * fitScale) / 2f
                        val fy = (first.y - model2D.bounds.minY) * fitScale + (canvasHeight - modelHeight * fitScale) / 2f
                        path.moveTo(fx, fy)

                        entity.points.drop(1).forEach { point ->
                            val px = (point.x - model2D.bounds.minX) * fitScale + (canvasWidth - modelWidth * fitScale) / 2f
                            val py = (point.y - model2D.bounds.minY) * fitScale + (canvasHeight - modelHeight * fitScale) / 2f
                            path.lineTo(px, py)
                        }

                        if (entity.closed) path.close()

                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(width = 2f)
                        )
                    }
                }

                is DxfEntity.LwPolyline -> {
                    if (entity.points.isNotEmpty()) {
                        val path = Path()
                        val first = entity.points.first()
                        val fx = (first.x - model2D.bounds.minX) * fitScale + (canvasWidth - modelWidth * fitScale) / 2f
                        val fy = (first.y - model2D.bounds.minY) * fitScale + (canvasHeight - modelHeight * fitScale) / 2f
                        path.moveTo(fx, fy)

                        entity.points.drop(1).forEach { point ->
                            val px = (point.x - model2D.bounds.minX) * fitScale + (canvasWidth - modelWidth * fitScale) / 2f
                            val py = (point.y - model2D.bounds.minY) * fitScale + (canvasHeight - modelHeight * fitScale) / 2f
                            path.lineTo(px, py)
                        }

                        if (entity.closed) path.close()

                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }
        }
    }
}
