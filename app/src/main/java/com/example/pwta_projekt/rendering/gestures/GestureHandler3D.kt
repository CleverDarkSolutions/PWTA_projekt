package com.example.pwta_projekt.rendering.gestures

import android.view.MotionEvent
import kotlin.math.sqrt

class GestureHandler3D {
    var rotationX = 0f
        private set
    var rotationY = 0f
        private set
    var scale = 1f
        private set
    var translateX = 0f
        private set
    var translateY = 0f
        private set

    private var previousX = 0f
    private var previousY = 0f
    private var previousDistance = 0f
    private var previousMidX = 0f
    private var previousMidY = 0f

    fun handleTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                previousY = event.y
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    previousDistance = calculateDistance(event)
                    val (midX, midY) = calculateMidpoint(event)
                    previousMidX = midX
                    previousMidY = midY
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1) {
                    // Single finger: rotation
                    val deltaX = event.x - previousX
                    val deltaY = event.y - previousY

                    rotationX += deltaY * ROTATION_SPEED
                    rotationY += deltaX * ROTATION_SPEED

                    // Clamp rotationX to avoid gimbal lock
                    rotationX = rotationX.coerceIn(-89f, 89f)

                    previousX = event.x
                    previousY = event.y
                } else if (event.pointerCount == 2) {
                    // Two fingers: zoom and pan
                    val currentDistance = calculateDistance(event)
                    val distanceDelta = currentDistance - previousDistance

                    val scaleMultiplier = 1f + (distanceDelta * ZOOM_SPEED)
                    scale *= scaleMultiplier
                    scale = scale.coerceIn(MIN_SCALE, MAX_SCALE)

                    previousDistance = currentDistance

                    // Pan
                    val (currentMidX, currentMidY) = calculateMidpoint(event)
                    val midDeltaX = currentMidX - previousMidX
                    val midDeltaY = currentMidY - previousMidY

                    translateX += midDeltaX * PAN_SPEED / scale
                    translateY -= midDeltaY * PAN_SPEED / scale

                    previousMidX = currentMidX
                    previousMidY = currentMidY
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount == 2) {
                    val remainingIndex = if (event.actionIndex == 0) 1 else 0
                    previousX = event.getX(remainingIndex)
                    previousY = event.getY(remainingIndex)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Reset tracking
            }
        }
        return true
    }

    fun reset() {
        rotationX = 0f
        rotationY = 0f
        scale = 1f
        translateX = 0f
        translateY = 0f
    }

    private fun calculateDistance(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return sqrt(dx * dx + dy * dy)
    }

    private fun calculateMidpoint(event: MotionEvent): Pair<Float, Float> {
        if (event.pointerCount < 2) return Pair(0f, 0f)
        val midX = (event.getX(0) + event.getX(1)) / 2f
        val midY = (event.getY(0) + event.getY(1)) / 2f
        return Pair(midX, midY)
    }

    companion object {
        private const val ROTATION_SPEED = 0.3f
        private const val ZOOM_SPEED = 0.005f
        private const val PAN_SPEED = 0.005f
        private const val MIN_SCALE = 0.1f
        private const val MAX_SCALE = 10f
    }
}
