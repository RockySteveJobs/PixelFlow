/**
 * 
 * PixelFlow | Copyright (C) 2017 Thomas Diewald - www.thomasdiewald.com
 * 
 * https://github.com/diwi/PixelFlow.git
 * 
 * A Processing/Java library for high performance GPU-Computing.
 * MIT License: https://opensource.org/licenses/MIT
 * 
 */



package FlowFieldParticles_Basic;

import java.util.Locale;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.flowfieldparticles.DwFlowFieldParticles;
import com.thomasdiewald.pixelflow.java.flowfieldparticles.DwFlowFieldParticles.SpawnRadial;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwFlowField;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwFilter;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.Merge.TexMad;
import processing.core.*;
import processing.opengl.PGraphics2D;



public class FlowFieldParticles_Basic extends PApplet {
  
  //
  //
  // Basic starter demo for a FlowFieldParticle simulation.
  //
  // Gravity, Particle Spawning, Animated Obstacles
  //
  // --- controls ----
  // mouse ... spawn particles
  // 'r'   ... reset Scene
  //
  //
  
  int viewport_w = 1680;
  int viewport_h = 1024;
  int viewport_x = 230;
  int viewport_y = 0;

  PGraphics2D pg_canvas;
  PGraphics2D pg_obstacles;

  DwPixelFlow context;
  
  DwFlowFieldParticles particles;
  DwFlowField ff_acc;

  
  public void settings() {
    size(viewport_w, viewport_h, P2D);
    smooth(0);
  }
  
  
  public void setup(){
    surface.setLocation(viewport_x, viewport_y);

    
    context = new DwPixelFlow(this);
    context.print();
    context.printGL();

    
    particles = new DwFlowFieldParticles(context, 1024 * 1024);
    particles.param.col_A = new float[]{0.10f, 0.50f, 0.80f, 5};
    particles.param.col_B = new float[]{0.05f, 0.25f, 0.40f, 0};
    particles.param.shader_collision_mult = 0.1f;
    particles.param.steps = 2;
    particles.param.velocity_damping  = 0.99f;
    particles.param.size_display   = 8;
    particles.param.size_collision = 8;
    particles.param.size_cohesion  = 4;
    particles.param.mul_coh = 2.00f;
    particles.param.mul_col = 2.00f;
    particles.param.mul_obs = 2.00f;
    
    pg_canvas = (PGraphics2D) createGraphics(width, height, P2D);
    pg_canvas.smooth(0);
    
    pg_obstacles = (PGraphics2D) createGraphics(width, height, P2D);
    pg_obstacles.smooth(8);

    // create gravity flow field
    PGraphics2D pg_gravity = (PGraphics2D) createGraphics(width, height, P2D);
    pg_gravity.smooth(0);
    pg_gravity.beginDraw();
    pg_gravity.background(0, 255, 0); // only green channel for gravity
    pg_gravity.endDraw();
    
    ff_acc = new DwFlowField(context);
    ff_acc.resize(width, height);
    TexMad ta = new TexMad(pg_gravity, -0.05f, 0);
    DwFilter.get(context).merge.apply(ff_acc.tex_vel, ta);


    frameRate(1000);
  }
  
  

  public void draw(){
    
    updateScene();
 
    spawnParticles();

    // update particle simulation
    particles.resizeWorld(width, height); 
    particles.createObstacleFlowField(pg_obstacles, new int[]{0,0,0,255}, false);
    particles.update(ff_acc);
    
    // render obstacles + particles
    pg_canvas.beginDraw(); 
    pg_canvas.background(255);
    pg_canvas.image(pg_obstacles, 0, 0);
    pg_canvas.endDraw();
    particles.display(pg_canvas);

    blendMode(REPLACE);
    image(pg_canvas, 0, 0);
    blendMode(BLEND);
    
    String txt_fps = String.format(Locale.ENGLISH, "[%s]   [%7.2f fps]   [particles %,d] ",  getClass().getSimpleName(), frameRate, particles.getCount() );
    surface.setTitle(txt_fps);
  }
  

  void updateScene(){

    int w = pg_obstacles.width;
    int h = pg_obstacles.height;
    float dim = 2* h/3f;
    
    pg_obstacles.beginDraw();
    pg_obstacles.clear();
    pg_obstacles.noStroke();
    pg_obstacles.blendMode(REPLACE);
    pg_obstacles.rectMode(CORNER);

    // border
    pg_obstacles.fill(0, 255);
    pg_obstacles.rect(0, 0, w, h);
    pg_obstacles.fill(0, 0);
    pg_obstacles.rect(25, 25, w-50, h-50);

    // animated obstacles
    pg_obstacles.rectMode(CENTER);
    pg_obstacles.pushMatrix();
    {
      pg_obstacles.translate(w/2, h-dim/2);
      pg_obstacles.rotate(frameCount/60f);
      pg_obstacles.fill(0, 255);
      pg_obstacles.rect(0, 0, dim,  30);
      pg_obstacles.rect(0, 0,  30, dim);
    }
    pg_obstacles.popMatrix();
    pg_obstacles.endDraw();
    
  }
  


  public void spawnParticles(){
    
    float px,py,vx,vy,radius;
    int count, vw, vh;
    
    vw = width;
    vh = height;

    count = 1;
    radius = 10;
    px = vw/2f;
    py = vh/4f;
    vx = 0;
    vy = 4;
    
    SpawnRadial sr = new SpawnRadial();
    sr.num(count);
    sr.dim(radius, radius);
    sr.pos(px, vh-1-py);
    sr.vel(vx, vy);
    particles.spawn(vw, vh, sr);

    if(mousePressed){     
      count = ceil(particles.getCount() * 0.01f);
      count = min(max(count, 1), 10000);  
      radius = ceil(sqrt(count));
      px = mouseX;
      py = mouseY;
      vx = (mouseX - pmouseX) * +5;
      vx = (mouseY - pmouseY) * -5;
      
      sr.num(count);
      sr.dim(radius, radius);
      sr.pos(px, vh-1-py);
      sr.vel(vx, vy);
      particles.spawn(vw, vh, sr);
    }
  }

  
  public void keyReleased(){
    if(key == 'r') particles.reset();
  }
  
  
  public static void main(String args[]) {
    PApplet.main(new String[] { FlowFieldParticles_Basic.class.getName() });
  }

}