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

        // TEST: Simple lines to confirm Canvas is working
        drawLine(
            color = Color.Red,
            start = Offset(100f, 100f),
            end = Offset(500f, 500f),
            strokeWidth = 5f
        )
        Log.d("Dxf2DViewer", "Drew red test line")

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

                    Log.d("Dxf2DViewer", "Line: ($x1,$y1) -> ($x2,$y2)")
                    drawLine(
                        color = Color.Blue,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 3f
                    )
                }

                is DxfEntity.Circle -> {
                    val cx = (entity.center.x - model2D.bounds.minX) * fitScale + (canvasWidth - modelWidth * fitScale) / 2f
                    val cy = (entity.center.y - model2D.bounds.minY) * fitScale + (canvasHeight - modelHeight * fitScale) / 2f
                    val r = entity.radius * fitScale

                    Log.d("Dxf2DViewer", "Circle: center($cx,$cy) r=$r")
                    drawCircle(
                        color = Color.Blue,
                        center = Offset(cx, cy),
                        radius = r,
                        style = Stroke(width = 3f)
                    )
                }

                is DxfEntity.Arc -> {
                    // Skip arcs for now - focus on basic shapes
                }

                is DxfEntity.Polyline -> {
                    // Skip polylines for now
                }

                is DxfEntity.LwPolyline -> {
                    // Skip polylines for now
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArc(
    color: Color,
    entity: DxfEntity.Arc,
    strokeWidth: Float
) {
    val cx = entity.center.x
    val cy = entity.center.y
    val radius = entity.radius

    val rect = Rect(
        left = cx - radius,
        top = cy - radius,
        right = cx + radius,
        bottom = cy + radius
    )

    // Angles are already in degrees (no conversion needed)
    val startAngleDegrees = entity.startAngle
    var sweepAngleDegrees = entity.endAngle - entity.startAngle

    // Handle wrap-around: if sweep is negative, add 360 to get the positive sweep
    if (sweepAngleDegrees < 0) {
        sweepAngleDegrees += 360f
    }

    drawArc(
        color = color,
        startAngle = startAngleDegrees,
        sweepAngle = sweepAngleDegrees,
        useCenter = false,
        topLeft = rect.topLeft,
        size = rect.size,
        style = Stroke(width = strokeWidth)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPolyline(
    color: Color,
    entity: DxfEntity.Polyline,
    strokeWidth: Float
) {
    if (entity.points.isEmpty()) return

    val path = Path()
    val first = entity.points.first()
    path.moveTo(first.x, first.y)

    entity.points.drop(1).forEach { point ->
        path.lineTo(point.x, point.y)
    }

    if (entity.closed && entity.points.size > 1) {
        path.close()
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLwPolyline(
    color: Color,
    entity: DxfEntity.LwPolyline,
    strokeWidth: Float
) {
    if (entity.points.isEmpty()) return

    val path = Path()
    val first = entity.points.first()
    path.moveTo(first.x, first.y)

    entity.points.drop(1).forEach { point ->
        path.lineTo(point.x, point.y)
    }

    if (entity.closed && entity.points.size > 1) {
        path.close()
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}
