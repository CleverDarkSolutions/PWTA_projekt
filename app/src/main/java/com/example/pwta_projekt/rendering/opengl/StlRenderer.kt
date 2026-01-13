package com.example.pwta_projekt.rendering.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.pwta_projekt.domain.models.Model3D
import com.example.pwta_projekt.rendering.gestures.GestureHandler3D
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class StlRenderer(
    private val model3D: Model3D,
    val gestureHandler: GestureHandler3D
) : GLSurfaceView.Renderer {

    private lateinit var shaderProgram: ShaderProgram
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var normalBuffer: FloatBuffer

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    private var vPositionHandle: Int = 0
    private var vNormalHandle: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set clear color
        GLES20.glClearColor(0.15f, 0.15f, 0.15f, 1.0f)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        // Enable back-face culling
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        // Create shader program
        shaderProgram = ShaderProgram(
            ShaderProgram.VERTEX_SHADER_CODE,
            ShaderProgram.FRAGMENT_SHADER_CODE
        )

        // Get attribute and uniform locations
        vPositionHandle = shaderProgram.getAttribLocation("vPosition")
        vNormalHandle = shaderProgram.getAttribLocation("vNormal")

        // Create buffers
        vertexBuffer = createFloatBuffer(model3D.vertices)
        normalBuffer = createFloatBuffer(model3D.normals)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        // Create projection matrix (perspective)
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 20f)

        // Create view matrix
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 8f,  // Camera position
            0f, 0f, 0f,  // Look at point
            0f, 1f, 0f   // Up vector
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear buffers
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Use shader program
        shaderProgram.use()

        // Build model matrix with transformations from gesture handler
        Matrix.setIdentityM(modelMatrix, 0)

        // Apply transformations
        Matrix.translateM(modelMatrix, 0, gestureHandler.translateX, gestureHandler.translateY, 0f)
        Matrix.scaleM(modelMatrix, 0, gestureHandler.scale, gestureHandler.scale, gestureHandler.scale)

        // Apply rotations
        Matrix.rotateM(modelMatrix, 0, gestureHandler.rotationX, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, gestureHandler.rotationY, 0f, 1f, 0f)

        // Center and scale model to fit view
        val maxDimension = model3D.bounds.maxDimension
        val scaleFactor = if (maxDimension > 0) 2f / maxDimension else 1f
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)

        // Translate to center
        Matrix.translateM(
            modelMatrix, 0,
            -model3D.bounds.centerX,
            -model3D.bounds.centerY,
            -model3D.bounds.centerZ
        )

        // Calculate MVP matrix
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

        // Set uniforms
        shaderProgram.setMatrix("uMVPMatrix", mvpMatrix)
        shaderProgram.setMatrix("uModelMatrix", modelMatrix)
        shaderProgram.setVector3("uLightDirection", 0.0f, 0.3f, 1.0f)
        shaderProgram.setVector4("uColor", 0.6f, 0.7f, 0.9f, 1.0f)

        // Enable vertex attributes
        GLES20.glEnableVertexAttribArray(vPositionHandle)
        GLES20.glEnableVertexAttribArray(vNormalHandle)

        // Bind buffers
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(
            vPositionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer
        )

        normalBuffer.position(0)
        GLES20.glVertexAttribPointer(
            vNormalHandle, 3, GLES20.GL_FLOAT, false, 12, normalBuffer
        )

        // Draw triangles
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, model3D.vertexCount)

        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(vPositionHandle)
        GLES20.glDisableVertexAttribArray(vNormalHandle)
    }

    fun release() {
        if (::shaderProgram.isInitialized) {
            shaderProgram.release()
        }
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(data)
            .apply { position(0) }
    }
}
