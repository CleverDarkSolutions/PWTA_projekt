package com.example.pwta_projekt.ui.components

import android.opengl.GLSurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pwta_projekt.domain.models.Model3D
import com.example.pwta_projekt.rendering.gestures.GestureHandler3D
import com.example.pwta_projekt.rendering.opengl.StlGLSurfaceView
import com.example.pwta_projekt.rendering.opengl.StlRenderer

@Composable
fun Stl3DViewer(
    model3D: Model3D,
    gestureHandler: GestureHandler3D,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val renderer = remember(model3D) {
        StlRenderer(model3D, gestureHandler)
    }

    DisposableEffect(Unit) {
        onDispose {
            renderer.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            StlGLSurfaceView(context, gestureHandler).apply {
                setRenderer(renderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }
        },
        onRelease = { glSurfaceView ->
            glSurfaceView.onPause()
        }
    )
}
