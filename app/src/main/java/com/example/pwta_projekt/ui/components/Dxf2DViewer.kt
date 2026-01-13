package com.example.pwta_projekt.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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

        // Calculate scale to fit model in view
        val modelWidth = model2D.bounds.width
        val modelHeight = model2D.bounds.height
        val modelCenterX = model2D.bounds.centerX
        val modelCenterY = model2D.bounds.centerY

        val scaleX = if (modelWidth > 0) canvasWidth * 0.8f / modelWidth else 1f
        val scaleY = if (modelHeight > 0) canvasHeight * 0.8f / modelHeight else 1f
        val autoScale = minOf(scaleX, scaleY)

        // Apply transformations
        translate(canvasWidth / 2 + offsetX, canvasHeight / 2 + offsetY) {
            scale(scale * autoScale, scale * autoScale) {
                rotate(rotation) {
                    translate(-modelCenterX, -modelCenterY) {
                        // Draw each entity
                        model2D.entities.forEach { entity ->
                            when (entity) {
                                is DxfEntity.Line -> drawLine(
                                    color = Color.White,
                                    start = Offset(entity.start.x, entity.start.y),
                                    end = Offset(entity.end.x, entity.end.y),
                                    strokeWidth = 2f / scale / autoScale
                                )

                                is DxfEntity.Arc -> drawArc(
                                    color = Color.White,
                                    entity = entity,
                                    strokeWidth = 2f / scale / autoScale
                                )

                                is DxfEntity.Circle -> drawCircle(
                                    color = Color.White,
                                    center = Offset(entity.center.x, entity.center.y),
                                    radius = entity.radius,
                                    style = Stroke(width = 2f / scale / autoScale)
                                )

                                is DxfEntity.Polyline -> drawPolyline(
                                    color = Color.White,
                                    entity = entity,
                                    strokeWidth = 2f / scale / autoScale
                                )

                                is DxfEntity.LwPolyline -> drawLwPolyline(
                                    color = Color.White,
                                    entity = entity,
                                    strokeWidth = 2f / scale / autoScale
                                )
                            }
                        }
                    }
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

    // Convert radians to degrees
    val startAngleDegrees = Math.toDegrees(entity.startAngle.toDouble()).toFloat()
    val sweepAngleDegrees = Math.toDegrees((entity.endAngle - entity.startAngle).toDouble()).toFloat()

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
