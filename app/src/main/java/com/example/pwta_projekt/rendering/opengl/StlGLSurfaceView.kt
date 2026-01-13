package com.example.pwta_projekt.rendering.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.pwta_projekt.rendering.gestures.GestureHandler3D

class StlGLSurfaceView(context: Context, private val gestureHandler: GestureHandler3D) : GLSurfaceView(context) {

    init {
        setEGLContextClientVersion(2)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = gestureHandler.handleTouch(event)
        requestRender()
        return handled
    }
}
