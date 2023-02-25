package graphics;

import core.Apiary;
import graphics.texture.TextureManager;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by Bailey on 11/17/2017.
 */
public class FBO {

    // This is the ID of our framebuffer
    int framebufferID;
    int texture2DTargetFramebufferID;

    int renderBufferID;

    int potentiallyMultisampledTextureID;
    int screenTextureID;

    int depthTextureID;

    // Sizing information used to set GLViewport when this object is resized.
    private int WIDTH;
    private int HEIGHT;
    private int originalWidth;
    private int originalHeight;

    // How many samples we are using.
    private int samples = 0;

    //If we unbind a frame buffer from within a framebuffer, we want to return the last item on the stack, not just 0
    private static LinkedList<Integer> bufferStack = new LinkedList<>();

    public FBO(){
        this(Apiary.getWindowWidth(), Apiary.getWindowHeight(), 0);
    }

    public FBO(int width, int height){
        this(width, height, 0);
    }

    public FBO(int width, int height, int samples){
        // Set the width
        this.WIDTH = width;
        this.HEIGHT = height;

        // set the samples
        this.samples = samples;

        //Check that FBO's are enabled on this system
        if(GL.getCapabilities().GL_EXT_framebuffer_object){
            framebufferID = GL46.glGenFramebuffers();
            texture2DTargetFramebufferID = GL46.glGenFramebuffers();

            renderBufferID = GL46.glGenRenderbuffers();

            GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, framebufferID);
            bufferStack.push(framebufferID);
            GL46.glDrawBuffer(GL46.GL_COLOR_ATTACHMENT0);

            potentiallyMultisampledTextureID = TextureManager.getInstance().genTexture();
            screenTextureID = TextureManager.getInstance().genTexture();
            depthTextureID = TextureManager.getInstance().genTexture();

            createTextureAttachment();
            createDepthBufferAttachment();
        }
        unbindFrameBuffer();
    }

    public void bindFrameBuffer(){
        if(isMultisampled()) {
            GL46.glEnable(GL46.GL_MULTISAMPLE);
        }
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, framebufferID);
        bufferStack.push(framebufferID);
        GL46.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL46.glClear(GL46.GL_COLOR_BUFFER_BIT | GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_STENCIL_BUFFER_BIT);
        double[] buffer = new double[4];
        GL46.glGetDoublev(GL46.GL_VIEWPORT, buffer);
        originalWidth  = (int) buffer[2];
        originalHeight = (int) buffer[3];
        GL46.glViewport(0, 0, WIDTH, HEIGHT);
    }

    public void unbindFrameBuffer(){
        // If we are miltisampled
        if(isMultisampled()) {
            // Disable the multisampling
            GL46.glDisable(GL46.GL_MULTISAMPLE);

            // Load a read and draw framebuffer into memory
            GL46.glBindFramebuffer(GL46.GL_READ_FRAMEBUFFER, framebufferID);
            GL46.glBindFramebuffer(GL46.GL_DRAW_FRAMEBUFFER, texture2DTargetFramebufferID);

            // Blit the MS frame buffer to the base framebuffer.
            GL46.glBlitFramebuffer(
                    // Size of source
                    0, 0, getFrameBufferWidth(), getFrameBufferHeight(),
                    // Size of dest
                    0, 0, getFrameBufferWidth(), getFrameBufferHeight(),
                    // Which bits to transfer, Here we want the color information and depth information to be transferred to the texture2D frameBuffer
                    GL46.GL_COLOR_BUFFER_BIT | GL46.GL_DEPTH_BUFFER_BIT ,
                    // Here we specify the filtering algorithm we want to use in the frame buffer blit code.
                    GL46.GL_NEAREST
            );

            // Unload our framebuffers
            GL46.glBindFramebuffer(GL46.GL_READ_FRAMEBUFFER, 0);
            GL46.glBindFramebuffer(GL46.GL_DRAW_FRAMEBUFFER, 0);

        }

        // Pop this buffer off the stack of buffers to be rendered.
        if(bufferStack.size() > 0) {
            bufferStack.pop();
        }
        // Reset the viewport
        GL46.glViewport(0, 0, originalWidth, originalHeight);
        // Flush
        GL46.glFlush();

        int newBufferID = 0;
        if(bufferStack.size() > 0){
            newBufferID = bufferStack.peek();
        }
        // If we have buffers in buffers load the next buffer.
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, newBufferID);
    }

    public final void resize(int width, int height){
        this.WIDTH  = width;
        this.HEIGHT = height;
        createTextureAttachment();
        createDepthBufferAttachment();
    }

    private void createTextureAttachment(){
        if(!isMultisampled()){
            GL46.glBindTexture(GL46.GL_TEXTURE_2D, potentiallyMultisampledTextureID);
            // IF this is not a multisampled FBO we will just create a regular FBO with the below buffer
            ByteBuffer buffer = ByteBuffer.allocateDirect((this.WIDTH * this.HEIGHT) * 4);
            for (int i = 0; i < (this.WIDTH * this.HEIGHT); i++) {
                buffer.put((byte) 0x00);
                buffer.put((byte) 0x00);
                buffer.put((byte) 0x00);
                buffer.put((byte) 0x00);
            }
            buffer.flip();
            GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA, this.WIDTH, this.HEIGHT, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, buffer);

            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);

            GL46.glFramebufferTexture(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT0, potentiallyMultisampledTextureID, 0);
            GL46.glBindTexture(GL46.GL_TEXTURE_2D, 0);

        }else{

            // Multisampling FBO
            GL46.glBindTexture(GL46.GL_TEXTURE_2D_MULTISAMPLE, potentiallyMultisampledTextureID);
            GL46.glTexImage2DMultisample(GL46.GL_TEXTURE_2D_MULTISAMPLE, samples, GL46.GL_RGB, this.WIDTH, this.HEIGHT, true);
            GL46.glBindTexture(GL46.GL_TEXTURE_2D_MULTISAMPLE, 0);
            GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT0, GL46.GL_TEXTURE_2D_MULTISAMPLE, potentiallyMultisampledTextureID, 0);

            // create a (also multisampled) renderbuffer object for depth and stencil attachments
            GL46.glBindRenderbuffer(GL46.GL_RENDERBUFFER, renderBufferID);
            GL46.glRenderbufferStorageMultisample(GL46.GL_RENDERBUFFER, samples, GL46.GL_DEPTH24_STENCIL8, this.WIDTH, this.HEIGHT);
            GL46.glBindRenderbuffer(GL46.GL_RENDERBUFFER, 0);
            GL46.glFramebufferRenderbuffer(GL46.GL_FRAMEBUFFER, GL46.GL_DEPTH_STENCIL_ATTACHMENT, GL46.GL_RENDERBUFFER, renderBufferID);

            int error = GL46.glCheckFramebufferStatus(GL46.GL_FRAMEBUFFER);
            if (error != GL46.GL_FRAMEBUFFER_COMPLETE){
                System.out.println("ERROR::FRAMEBUFFER:: Intermediate framebuffer is not complete!" + error);
            }

            // configure second post-processing framebuffer
            GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, texture2DTargetFramebufferID);
            GL46.glBindTexture(GL46.GL_TEXTURE_2D, screenTextureID);
            // IF this is not a multisampled FBO we will just create a regular FBO with the below buffer
            ByteBuffer buffer = ByteBuffer.allocateDirect((this.WIDTH * this.HEIGHT) * 4);
            for (int i = 0; i < (this.WIDTH * this.HEIGHT); i++) {
                buffer.put((byte) 0x00);
                buffer.put((byte) 0x00);
                buffer.put((byte) 0x00);
                buffer.put((byte) 0x00);
            }
            buffer.flip();
            GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA16F, this.WIDTH, this.HEIGHT, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, buffer);

            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);

            GL46.glFramebufferTexture(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT0, screenTextureID, 0);
            GL46.glBindTexture(GL46.GL_TEXTURE_2D, 0);

            error = GL46.glCheckFramebufferStatus(GL46.GL_FRAMEBUFFER);
            if (GL46.glCheckFramebufferStatus(GL46.GL_FRAMEBUFFER) != GL46.GL_FRAMEBUFFER_COMPLETE){
                System.out.println("ERROR::FRAMEBUFFER:: Intermediate framebuffer is not complete!" + error);
            }

            GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, 0);
        }
    }

    private void createDepthBufferAttachment(){
        if(!isMultisampled()) {
            GL46.glBindTexture(GL46.GL_TEXTURE_2D, depthTextureID);
            ByteBuffer buffer = ByteBuffer.allocateDirect((this.WIDTH * this.HEIGHT) * 4);
            for (int i = 0; i < (this.WIDTH * this.HEIGHT); i++) {
                buffer.put((byte) 0x00);
                buffer.put((byte) 0x00);
                buffer.put((byte) 0x00);
                buffer.put((byte) 0x00);
            }
            buffer.flip();

            GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_DEPTH_COMPONENT32, this.WIDTH, this.HEIGHT, 0, GL46.GL_DEPTH_COMPONENT, GL46.GL_FLOAT, buffer);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_NEAREST);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_NEAREST);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_BORDER);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_BORDER);
            GL46.glTexParameterfv(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_BORDER_COLOR, new float[]{1.0f, 1.0f, 1.0f, 1.0f});

            GL46.glFramebufferTexture(GL46.GL_FRAMEBUFFER, GL46.GL_DEPTH_ATTACHMENT, depthTextureID, 0);
        }
    }


    public void cleanUp(){

        GL46.glDeleteFramebuffers(framebufferID);

        if(texture2DTargetFramebufferID > 0) {
            GL46.glDeleteFramebuffers(texture2DTargetFramebufferID);
        }
        if(renderBufferID > 0) {
            GL46.glDeleteRenderbuffers(renderBufferID);
        }

        if(potentiallyMultisampledTextureID > 0) {
            GL46.glDeleteTextures(potentiallyMultisampledTextureID);
        }

        if(screenTextureID > 0) {
            GL46.glDeleteTextures(screenTextureID);
        }

        if(depthTextureID > 0) {
            GL46.glDeleteTextures(depthTextureID);
        }
    }


    public int getTextureID(){
        return isMultisampled() ? screenTextureID : potentiallyMultisampledTextureID;
    }

    public int getDepthTexture(){
        return depthTextureID;
    }

    public int getFrameBufferWidth() {
        return WIDTH;
    }

    public int getFrameBufferHeight() {
        return HEIGHT;
    }

    public int getNumberOfSamplesUsed(){
        if(isMultisampled()){
            return samples;
        }else{
            return 0;
        }
    }

    public boolean isMultisampled(){
        return this.samples > 0;
    }
}
