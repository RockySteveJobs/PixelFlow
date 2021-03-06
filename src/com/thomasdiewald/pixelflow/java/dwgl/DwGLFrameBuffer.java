/**
 * 
 * PixelFlow | Copyright (C) 2016 Thomas Diewald - http://thomasdiewald.com
 * 
 * A Processing/Java library for high performance GPU-Computing (GLSL).
 * MIT License: https://opensource.org/licenses/MIT
 * 
 */




package com.thomasdiewald.pixelflow.java.dwgl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLES3;

public class DwGLFrameBuffer {
  public GL2ES2 gl;
  

  public int[] HANDLE_fbo = null;
  public int[] bind_color_attachments = new int[0]; // currently bound rendertargets
  public int[] bind_targets           = new int[0]; // currently bound rendertargets
  
  public int max_color_attachments;
  public int max_draw_buffers;
  public int max_bind;
  public DwGLFrameBuffer(GL2ES2 gl){
    allocate(gl);
  }
  
  public void release(){
    if(gl != null){
      if(HANDLE_fbo != null) gl.glDeleteFramebuffers(1, HANDLE_fbo, 0); 
      HANDLE_fbo = null;
      gl = null;
    }
  }
  

  public void allocate(GL2ES2 gl){
    if(HANDLE_fbo == null){
      HANDLE_fbo = new int[1];
      this.gl = gl;
      gl.glGenFramebuffers(1, HANDLE_fbo, 0);
      
      int[] buf = new int[1];
      gl.glGetIntegerv(GL2.GL_MAX_COLOR_ATTACHMENTS, buf, 0);
      max_color_attachments = buf[0];
      gl.glGetIntegerv(GL2.GL_MAX_DRAW_BUFFERS, buf, 0);
      max_draw_buffers = buf[0];
      
      max_bind = Math.min(max_draw_buffers, max_color_attachments);
    }
  }
  
  
  public void bind(){
    gl.glBindFramebuffer(GL2ES2.GL_FRAMEBUFFER, HANDLE_fbo[0]);
  }
  

  // textures must be of the same size
  public void bind(int ... HANDLE_tex){
    
    if(IS_ACTIVE){
      unbind(); // unbind, in case of bind() is called consecutively
    }
    
    int count = HANDLE_tex.length;  
    if(count > max_bind){
      System.out.println("WARNING: DwGLFrameBuffer.bind(...) number of textures exceeds max limit: "+count+" > "+max_bind);
      count = max_bind;
    }
    bind_color_attachments = new int[count];
    bind_targets           = new int[count];
    
    bind();
    for(int i = 0; i < count; i++){
      bind_color_attachments[i] = GL2ES2.GL_COLOR_ATTACHMENT0 + i;
      bind_targets          [i] = GL2ES2.GL_TEXTURE_2D;
      gl.glFramebufferTexture2D(GL2ES2.GL_FRAMEBUFFER, bind_color_attachments[i], GL2ES2.GL_TEXTURE_2D, HANDLE_tex[i], 0);
    }
    
    gl.glDrawBuffers(bind_color_attachments.length, bind_color_attachments, 0);
    IS_ACTIVE = true;
  }
  
  
  public boolean IS_ACTIVE = false;
  
   
  
  public void bind(DwGLTexture ... tex){
   
    if(IS_ACTIVE){
      unbind(); // unbind, in case of bind() is called consecutively
    }
    
    int count = tex.length;  
    if(count > max_bind){
      System.out.println("WARNING: DwGLFrameBuffer.bind(...) number of textures exceeds max limit: "+count+" > "+max_bind);
      count = max_bind;
    }
    bind_color_attachments = new int[count];
    bind_targets           = new int[count];
    
    gl.glBindFramebuffer(GL2ES2.GL_FRAMEBUFFER, HANDLE_fbo[0]);
    for(int i = 0; i < count; i++){
      bind_color_attachments[i] = GL2ES2.GL_COLOR_ATTACHMENT0 + i;
      bind_targets          [i] = tex[i].target;
      gl.glFramebufferTexture2D(GL2ES2.GL_FRAMEBUFFER, bind_color_attachments[i], tex[i].target, tex[i].HANDLE[0], 0);
    }
    
    
    gl.glDrawBuffers(bind_color_attachments.length, bind_color_attachments, 0);
    IS_ACTIVE = true;
  }
  
  public void bind(DwGLTexture3D[] tex, int[] layer){
   
    if(IS_ACTIVE){
      unbind(); // unbind, in case of bind() is called consecutively
    }
    
    int count = tex.length;  
    if(count > max_bind){
      System.out.println("WARNING: DwGLFrameBuffer.bind(...) number of textures exceeds max limit: "+count+" > "+max_bind);
      count = max_bind;
    }
    bind_color_attachments = new int[count];
    bind_targets           = new int[count];
    
    gl.glBindFramebuffer(GL2ES2.GL_FRAMEBUFFER, HANDLE_fbo[0]);
    for(int i = 0; i < count; i++){
      bind_color_attachments[i] = GL2ES2.GL_COLOR_ATTACHMENT0 + i;
      bind_targets          [i] = tex[i].target;
      gl.glFramebufferTexture3D(GL2ES2.GL_FRAMEBUFFER, bind_color_attachments[i], tex[i].target, tex[i].HANDLE[0], 0, layer[i]);
    }
    
    gl.glDrawBuffers(bind_color_attachments.length, bind_color_attachments, 0);
    IS_ACTIVE = true;
  }
  
  
  public void unbind(){ 
    for(int i = 0; i < bind_color_attachments.length; i++){
      if(   bind_targets[i] == GL2.GL_TEXTURE_2D_ARRAY
         || bind_targets[i] == GL2.GL_TEXTURE_3D){
        gl.glFramebufferTexture3D(GL2ES2.GL_FRAMEBUFFER, bind_color_attachments[i], bind_targets[i], 0, 0, 0);
      } else {
        gl.glFramebufferTexture2D(GL2ES2.GL_FRAMEBUFFER, bind_color_attachments[i], bind_targets[i], 0, 0);
      }
    }
    bind_color_attachments = new int[0];
    gl.glBindFramebuffer(GL2ES2.GL_FRAMEBUFFER, 0);
    IS_ACTIVE = false;
  }
  
  public boolean isActive(){
    return IS_ACTIVE;
  }
  
  
  public void clearTexture(float r, float g, float b, float a, DwGLTexture ... tex){
    bind(tex);
    int w = tex[0].w();
    int h = tex[0].h();
    gl.glViewport(0, 0, w, h);
    gl.glColorMask(true, true, true, true);
    gl.glClearColor(r,g,b,a);
    gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
    unbind();
    DwGLError.debug(gl, "DwGLFrameBuffer.clearTexture");
  }
  
  
  
  public void clearTexture(float r, float g, float b, float a, DwGLTexture3D[] tex, int[] layer){
    bind(tex, layer);
    int w = tex[0].w();
    int h = tex[0].h();
    gl.glViewport(0, 0, w, h);
    gl.glColorMask(true, true, true, true);
    gl.glClearColor(r,g,b,a);
    gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
    unbind();
    DwGLError.debug(gl, "DwGLFrameBuffer.clearTexture");
  }
  
  
//  public void clearTexture(float v, DwGLTexture ... tex){
//    clearTexture(v,v,v,v,tex);
//  }
//  public void clearTexture(float v, DwGLTexture3D[] tex, int[] layer){
//    clearTexture(v,v,v,v,tex);
//  }



  
  
  
  
  public void setRenderBuffer(int HANDLE_rbo, boolean depth, boolean stencil){
    boolean is_active = isActive();
    if(!is_active) bind();
  
    if(depth  ) gl.glFramebufferRenderbuffer(GLES3.GL_FRAMEBUFFER, GLES3.GL_DEPTH_ATTACHMENT  , GLES3.GL_RENDERBUFFER, HANDLE_rbo);
    if(stencil) gl.glFramebufferRenderbuffer(GLES3.GL_FRAMEBUFFER, GLES3.GL_STENCIL_ATTACHMENT, GLES3.GL_RENDERBUFFER, HANDLE_rbo);
    
    if(!is_active) unbind();
  }
  
}
