package com.cam001.gles;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.cam001.util.DebugUtil;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.opengl.GLES20;


public class ShaderUtil 
{
	
   private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);

        if (shader != 0) 
        {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) 
            {
            	DebugUtil.logE("ES20_ERROR", "Could not compile shader " + shaderType + ":");
            	DebugUtil.logE("ES20_ERROR", GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;      
            }  
        }
        return shader;
    }
    
   public static int createProgram(String vertexSource, String fragmentSource) 
   {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
    	checkGlError("loadShader GL_VERTEX_SHADER");
        if (vertexShader == 0) 
        {
        	throw new RuntimeException("loadShader GL_VERTEX_SHADER return 0");
        }
        
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        checkGlError("loadShader GL_FRAGMENT_SHADER");
        if (pixelShader == 0) 
        {
        	throw new RuntimeException("loadShader GL_FRAGMENT_SHADER return 0");
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) 
        {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) 
            {
            	DebugUtil.logE("ES20_ERROR", "Could not link program: ");
            	DebugUtil.logE("ES20_ERROR", GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
            GLES20.glDeleteShader(vertexShader);
            GLES20.glDeleteShader(pixelShader);
        }
        return program;
    }
    
   public static void checkGlError(String op) 
   {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) 
        {
        	String err = String.format("glError: 0x%X", error);
        	DebugUtil.logE("ES20_ERROR", op + ": " + err);
            throw new RuntimeException(op + ": "+err);
        }
   }
   
   public static void printGLString(String name, int s) {
	    String v =  GLES20.glGetString(s);
	    DebugUtil.logV("", String.format("GL %s = %s\n", name, v));
	}
   
   public static int createTexture() {
	   int[] tex = new int[1];
	   GLES20.glGenTextures(1, tex, 0);
	   int texName = tex[0];
	   GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texName);
	   GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
	   GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
	   GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
	   GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
	   return texName;
   }
   
   public static FloatBuffer floatToBuffer(float[] a)
   {
       ByteBuffer mbb = ByteBuffer.allocateDirect(a.length * 4);
       mbb.order(ByteOrder.nativeOrder());
       FloatBuffer fBuf = mbb.asFloatBuffer();
       fBuf.put(a);
       fBuf.position(0);
       return fBuf;
   }
   
   public static String loadFromAssetsFile(String fname,Resources r)
   {
   	String result=null;    	
   	try
   	{
   		InputStream in=r.getAssets().open(fname);
			int ch=0;
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    while((ch=in.read())!=-1)
		    {
		      	baos.write(ch);
		    }      
		    byte[] buff=baos.toByteArray();
		    baos.close();
		    in.close();
   		result=new String(buff,"UTF-8"); 
   		result=result.replaceAll("\\r\\n","\n");
   	}
   	catch(Exception e)
   	{
   		e.printStackTrace();
   	}    	
   	return result;
   }

    static {
        try {
            System.loadLibrary("tsutils");
        } catch (UnsatisfiedLinkError e) {
            String path = "/data/thundersoft/makeup/";
            System.load(path+"libtsutils.so");
        }
    }

   public static native void glReadPixelsToBitmap(Bitmap bmp);
   public static native void glTexImage2DJpeg(byte[] jpg, Point size);
   public static native void glTexImage2DBitmap(Bitmap bmp);
   public static native void glReadPixelsToFile(Bitmap warterMark, int outWidth, int outHeight, String outPath);
    public static native byte[] glReadPixelsToJpeg(int width, int height, byte[] buf);
   public static native void glTexImage2D(byte[] data, int start, int format, int width, int height);
}
