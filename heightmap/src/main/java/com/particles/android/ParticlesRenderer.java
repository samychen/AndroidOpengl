/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.particles.android;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_LESS;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLSurfaceView.Renderer;

import com.particles.android.objects.Heightmap;
import com.particles.android.objects.ParticleShooter;
import com.particles.android.objects.ParticleSystem;
import com.particles.android.objects.Skybox;
import com.particles.android.programs.HeightmapShaderProgram;
import com.particles.android.programs.ParticleShaderProgram;
import com.particles.android.programs.SkyboxShaderProgram;
import com.particles.android.util.Geometry.Point;
import com.particles.android.util.Geometry.Vector;
import com.particles.android.util.MatrixHelper;
import com.particles.android.util.TextureHelper;

public class ParticlesRenderer implements Renderer {    
    private final Context context;

    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewMatrixForSkybox = new float[16];
    private final float[] projectionMatrix = new float[16];        
    
    private final float[] tempMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private HeightmapShaderProgram heightmapProgram;
    private Heightmap heightmap;
      
    private SkyboxShaderProgram skyboxProgram;
    private Skybox skybox;   
    
    private ParticleShaderProgram particleProgram;      
    private ParticleSystem particleSystem;
    private ParticleShooter redParticleShooter;
    private ParticleShooter greenParticleShooter;
    private ParticleShooter blueParticleShooter;     

    private long globalStartTime;    
    private int particleTexture;
    private int skyboxTexture;
    
    private float xRotation, yRotation;  

    public ParticlesRenderer(Context context) {
        this.context = context;
    }

    public void handleTouchDrag(float deltaX, float deltaY) {
        xRotation += deltaX / 16f;
        yRotation += deltaY / 16f;
        if (yRotation < -90) {
            yRotation = -90;
        } else if (yRotation > 90) {
            yRotation = 90;
        } 
        // Setup view matrix
        updateViewMatrices();        
    }
    
    private void updateViewMatrices() {
        setIdentityM(viewMatrix, 0);
        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f);
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f);
        System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.length);
        // We want the translation to apply to the regular view matrix, and not
        // the skybox.
        translateM(viewMatrix, 0, 0, -1.5f, -5f);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);  
        glEnable(GL_DEPTH_TEST);//打开深度缓冲区功能,如果片段比已经存在的片段更近，就绘制它，否则丢弃
        glEnable(GL_CULL_FACE);//剔除，关闭两面绘制
        
        heightmapProgram = new HeightmapShaderProgram(context);
        heightmap = new Heightmap(((BitmapDrawable)context.getResources()
            .getDrawable(R.drawable.heightmap)).getBitmap());
        
        skyboxProgram = new SkyboxShaderProgram(context);
        skybox = new Skybox();      
        
        particleProgram = new ParticleShaderProgram(context);        
        particleSystem = new ParticleSystem(10000);        
        globalStartTime = System.nanoTime();
        
        final Vector particleDirection = new Vector(0f, 0.5f, 0f);              
        final float angleVarianceInDegrees = 5f; 
        final float speedVariance = 1f;
            
        redParticleShooter = new ParticleShooter(
            new Point(-1f, 0f, 0f), 
            particleDirection,                
            Color.rgb(255, 50, 5),            
            angleVarianceInDegrees, 
            speedVariance);
        greenParticleShooter = new ParticleShooter(
            new Point(0f, 0f, 0f), 
            particleDirection,
            Color.rgb(25, 255, 25),            
            angleVarianceInDegrees, 
            speedVariance);
        blueParticleShooter = new ParticleShooter(
            new Point(1f, 0f, 0f), 
            particleDirection,
            Color.rgb(5, 50, 255),            
            angleVarianceInDegrees, 
            speedVariance); 
        particleTexture = TextureHelper.loadTexture(context, R.drawable.particle_texture);
        skyboxTexture = TextureHelper.loadCubeMap(context,
            new int[] { R.drawable.left, R.drawable.right,
                        R.drawable.bottom, R.drawable.top, 
                        R.drawable.front, R.drawable.back});
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {                
        glViewport(0, 0, width, height);        
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
            / (float) height, 1f, 100f);
        /*
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
            / (float) height, 1f, 10f);
        */
        updateViewMatrices();
    }

    @Override    
    public void onDrawFrame(GL10 glUnused) {
        /*
        glClear(GL_COLOR_BUFFER_BIT);
         */
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//告诉OpenGL新帧上要清空深度缓冲
        //注意绘制顺序
        drawHeightmap();
        drawSkybox();        
        drawParticles();
    }
    private void drawHeightmap() {
        setIdentityM(modelMatrix, 0);  
        // Expand the heightmap's dimensions, but don't expand the height as
        // much so that we don't get insanely tall mountains.
        scaleM(modelMatrix, 0, 100f, 10f, 100f);
        updateMvpMatrix();
        heightmapProgram.useProgram();
        heightmapProgram.setUniforms(modelViewProjectionMatrix);
        heightmap.bindData(heightmapProgram);
        heightmap.draw(); 
    }
    
    private void drawSkybox() {   
        setIdentityM(modelMatrix, 0);
        updateMvpMatrixForSkybox();
        //https://blog.csdn.net/junzia/article/details/52820177
        //改变深度测试算法。默认情况下使用GL_LESS，新片段比任何已存在片段近或者比远平面近，就让他通过测试。改为GL_LEQUAL，新片段与已存在片段相比较接近或者二者在同等距离处，就让他通过测试
        glDepthFunc(GL_LEQUAL); // This avoids problems with the skybox itself getting clipped.
        skyboxProgram.useProgram();
        skyboxProgram.setUniforms(modelViewProjectionMatrix, skyboxTexture);
        skybox.bindData(skyboxProgram);
        skybox.draw();
        glDepthFunc(GL_LESS);
    }
   
    private void drawParticles() {        
        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;
        redParticleShooter.addParticles(particleSystem, currentTime, 1);
        greenParticleShooter.addParticles(particleSystem, currentTime, 1);              
        blueParticleShooter.addParticles(particleSystem, currentTime, 1);              
        setIdentityM(modelMatrix, 0);
        updateMvpMatrix();
        
        glDepthMask(false);//在保持深度测试的同时禁用深度更新，粒子将针对地面进行测试，但其测试结果不会被写入深度缓冲区，这样就不会彼此遮挡了
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        
        particleProgram.useProgram();
        particleProgram.setUniforms(modelViewProjectionMatrix, currentTime, particleTexture);
        particleSystem.bindData(particleProgram);
        particleSystem.draw(); 
        
        glDisable(GL_BLEND);
        glDepthMask(true);
    }
    
    private void updateMvpMatrix() {
        multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
    }   
    private void updateMvpMatrixForSkybox() {
        multiplyMM(tempMatrix, 0, viewMatrixForSkybox, 0, modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
    }
}