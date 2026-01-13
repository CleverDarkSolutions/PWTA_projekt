package com.example.pwta_projekt.rendering.opengl

import android.opengl.GLES20
import android.util.Log

class ShaderProgram(vertexShaderCode: String, fragmentShaderCode: String) {
    var programId: Int = 0
        private set

    init {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)

            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                val error = GLES20.glGetProgramInfoLog(it)
                Log.e("ShaderProgram", "Error linking program: $error")
                GLES20.glDeleteProgram(it)
                throw RuntimeException("Error linking shader program")
            }
        }
    }

    fun use() {
        GLES20.glUseProgram(programId)
    }

    fun getAttribLocation(name: String): Int {
        return GLES20.glGetAttribLocation(programId, name)
    }

    fun getUniformLocation(name: String): Int {
        return GLES20.glGetUniformLocation(programId, name)
    }

    fun setMatrix(name: String, matrix: FloatArray) {
        val location = getUniformLocation(name)
        GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0)
    }

    fun setVector3(name: String, x: Float, y: Float, z: Float) {
        val location = getUniformLocation(name)
        GLES20.glUniform3f(location, x, y, z)
    }

    fun setVector4(name: String, x: Float, y: Float, z: Float, w: Float) {
        val location = getUniformLocation(name)
        GLES20.glUniform4f(location, x, y, z, w)
    }

    fun release() {
        GLES20.glDeleteProgram(programId)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                val error = GLES20.glGetShaderInfoLog(shader)
                Log.e("ShaderProgram", "Error compiling shader: $error")
                GLES20.glDeleteShader(shader)
                throw RuntimeException("Error compiling shader")
            }
        }
    }

    companion object {
        const val VERTEX_SHADER_CODE = """
            attribute vec4 vPosition;
            attribute vec3 vNormal;
            uniform mat4 uMVPMatrix;
            uniform mat4 uModelMatrix;
            varying vec3 v_Normal;

            void main() {
                gl_Position = uMVPMatrix * vPosition;
                v_Normal = normalize((uModelMatrix * vec4(vNormal, 0.0)).xyz);
            }
        """

        const val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            varying vec3 v_Normal;
            uniform vec3 uLightDirection;
            uniform vec4 uColor;

            void main() {
                float diffuse = max(dot(v_Normal, uLightDirection), 0.0);
                vec3 ambient = vec3(0.3, 0.3, 0.3);
                vec3 finalColor = ambient + diffuse * uColor.rgb;
                gl_FragColor = vec4(finalColor, uColor.a);
            }
        """
    }
}
