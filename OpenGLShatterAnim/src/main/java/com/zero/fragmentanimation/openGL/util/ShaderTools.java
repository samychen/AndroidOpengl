package com.zero.fragmentanimation.openGL.util;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

/**
 * 着色器工具类
 */
public class ShaderTools {
    
    private static final String TAG = "ShaderTools";
    
    /**
     * 创建程序
     * @param context
     * @param rawVertexSource  顶点着色器代码文件
     * @param rawFragmentSource  片元着色器代码文件
     * @return
     */
    public static int createProgram(Context context, int rawVertexSource, int rawFragmentSource) {
        int vertex = loadShader(context, GLES20.GL_VERTEX_SHADER, rawVertexSource);
        if (vertex == 0) {
            return 0;
        }
        int fragment = loadShader(context, GLES20.GL_FRAGMENT_SHADER, rawFragmentSource);
        if (fragment == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertex);
            Log.d(TAG, "Attach Vertex Shader");
            GLES20.glAttachShader(program, fragment);
            Log.d(TAG, "Attach Fragment Shader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program:" + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    /**
     * 根据资源文件加载着色器程序
     * @param context
     * @param shaderType
     * @param resName
     * @return
     */
    public static int loadShader(Context context, int shaderType, int resName) {
        return loadShader(shaderType, RawTools.readTextFileFromRawResource(context, resName));
    }

    /**
     * 根据着色器代码加载着色器程序
     * @param shaderType
     * @param source
     * @return
     */
    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (0 != shader) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader:" + shaderType);
                Log.e(TAG, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

}
