/* I declare that this code is my own work */
/* Author Dominic Jolley - djolley1@sheffield.ac.uk */
/*
  Base structure taken from M03_GLEventListener
    Used for imports, init function, reshape function, and delay function

  User controlled camera taken from tutorials

  Classes taken from tutorials include
    Camera
    Cube
    Material
    Mesh
    Model (edited)
    ModelNode
    NameNode
    SGNode
    Shader
    Sphere
    TextureLibrary
    TransformNode
    TwoTriangles


 */

import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;

public class main_GLEventListener implements GLEventListener {

  private static final boolean DISPLAY_SHADERS = false;

  public main_GLEventListener(Camera camera) {
    this.camera = camera;
    this.camera.setPosition(new Vec3(4f,12f,18f));
  }

  // ***************************************************
  /*
   * METHODS DEFINED BY GLEventListener
   */

  /* Initialisation */
  public void init(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthFunc(GL.GL_LESS);
    gl.glFrontFace(GL.GL_CCW);    // default is 'CCW'
    gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled'
    gl.glCullFace(GL.GL_BACK);   // default is 'back', assuming CCW
    initialise(gl);
    startTime = getSeconds();
  }

  /* Called to indicate the drawing surface has been moved and/or resized  */
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL3 gl = drawable.getGL().getGL3();
    gl.glViewport(x, y, width, height);
    float aspect = (float)width/(float)height;
    camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
  }

  /* Draw */
  public void display(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    render(gl);
  }

  /* Clean up memory */
  public void dispose(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    light.dispose(gl);
    floor.dispose(gl);
    wall.dispose(gl);
    cube.dispose(gl);
    hatCube.dispose(gl);
    snowSphere.dispose(gl);
    stoneSphere.dispose(gl);
    carrotSphere.dispose(gl);
    lamppostSphere.dispose(gl);
    hatSphere.dispose(gl);

  }

  /* INTERACTION */
    // ***************************************************

    /* These functions interact with the buttons the user can interact with
        Each sets a variable to true or false which is then used later in the
        render function to either run or not run the animation
    */

    private boolean lights = true;
    private boolean spotlight = true;
    private boolean spotRotate = false;
    private boolean rock = false;
    private boolean roll = false;
    private boolean slide = false;
    private boolean rockRollSlide = false;
    private double savedTime = 0;

    public void lightsOnOff() {
      if(lights) lights = false;
      else lights = true;
    }

    public void spotlightOnOff() {
      if(spotlight) spotlight = false;
      else spotlight = true;
    }

    public void spotlightRotate(){
      if(!spotRotate) spotRotate = true;
      else spotRotate = false;
    }

    public void rockAnimation() {
      rock = true;
      roll = false;
      slide = false;
      rockRollSlide = false;
      startTime = getSeconds()-savedTime;
    }

    public void rollAnimation() {
      rock = false;
      roll = true;
      slide = false;
      rockRollSlide = false;
      startTime = getSeconds()-savedTime;
    }

    public void slideAnimation() {
      rock = false;
      roll = false;
      slide = true;
      rockRollSlide = false;
      startTime = getSeconds()-savedTime;
    }

    public void rrsAnimation() {
      rock = true;
      roll = true;
      slide = true;
      //rockRollSlide = true;
      startTime = getSeconds()-savedTime;
    }

    public void resetSnowman() {  //Sets all animations to false and resets the snowmans position to its origin
        rock = false;
        roll = false;
        slide = false;
        rockRollSlide = false;
        bodyTranslation.setTransform(Mat4Transform.translate(0, 0, 0));
        bodyTranslation.update();
        bodyRotate.setTransform(Mat4Transform.translate(0, 0, 0));
        bodyRotate.update();
        headRotate.setTransform(Mat4Transform.translate(0, 0, 0));
        headRotate.update();
      }
  // ***************************************************

  /* THE SCENE    */

  private Camera camera;
  private Mat4 perspective;
  private Model floor, wall, cube, hatCube, sphere, snowSphere, stoneSphere, carrotSphere, lamppostSphere, hatSphere;
  private Light light;
  private SGNode snowmanRoot, lampRoot;

  private TransformNode bodyTranslation, bodyRotate, headTranslation, headRotate, lampHeadRotate, lampLightPos;

  private float spotlightX;
  private float spotlightY;
  private float spotlightZ;
  private float spotDirectionX;
  private float spotDirectionY;
  private float spotDirectionZ;

  private void initialise(GL3 gl) {
    //Initialise textures
    int[] textureId0 = TextureLibrary.loadTexture(gl, "textures/road_dirt_road_0022_01_s.jpg");      //Floor texture
    int[] textureId1 = TextureLibrary.loadTexture(gl, "textures/2148.jpg");                          //wall background
    int[] textureId2 = TextureLibrary.loadTexture(gl, "textures/11097.jpg");                         //Wall snow overlay
    int[] textureId3 = TextureLibrary.loadTexture(gl, "textures/snow_texture1550.jpg");              //Snoman dirty snow texture
    int[] textureId4 = TextureLibrary.loadTexture(gl, "textures/texture_rock.jpg");                  //Snowman rock buttons, eyes, mouth textures
    int[] textureId5 = TextureLibrary.loadTexture(gl, "textures/carrot.jpg");                        //Carrot Texture
    int[] textureId6 = TextureLibrary.loadTexture(gl, "textures/brushedsteel4.jpg");                 //Lamppost rusty metal texture
    int[] textureId7 = TextureLibrary.loadTexture(gl, "textures/snow_clipart.jpg", GL.GL_REPEAT, GL.GL_REPEAT, GL.GL_LINEAR, GL.GL_LINEAR);                    //Cube normal texture
    int[] textureId8 = TextureLibrary.loadTexture(gl, "textures/snow_clipart_spec.jpg", GL.GL_REPEAT, GL.GL_REPEAT, GL.GL_LINEAR, GL.GL_LINEAR);           //Cube specular map
    int[] textureId9 = TextureLibrary.loadTexture(gl, "textures/350.jpg");                           //hat fur texture
    int[] textureId10 = TextureLibrary.loadTexture(gl, "textures/32107.jpg");                        //hat body texture

    light = new Light(gl);
    light.setCamera(camera);

    //Floor
      Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
      Shader shader = new Shader(gl, "vs_floor.txt", "fs_floor.txt");
      Material material = new Material(new Vec3(0.4f, 0.5f, 0.5f), new Vec3(0.5f, 0.5f, 0.5f), new Vec3(0.7f, 0.7f, 0.7f), 4.0f);
      Mat4 modelMatrix = Mat4Transform.scale(30,1f,16);
      floor = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId0);

    //Wall
      mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
      shader = new Shader(gl, "vs_wall.txt", "fs_wall.txt");
      material = new Material(new Vec3(0.4f, 0.5f, 0.5f), new Vec3(0.4f, 0.5f, 0.5f), new Vec3(0.4f, 0.7f, 0.7f), 4.0f);
      // diffuse texture only for this model
      wall = new Model(gl, camera, light, shader, material, new Mat4(1), mesh, textureId1, textureId7);

    //Sphere shaped objects with different material properties (only shinyness changes as each object is texture mapped)
      mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
      shader = new Shader(gl, "vs_spheres.txt", "fs_spheres.txt");
      modelMatrix = Mat4.multiply(Mat4Transform.scale(4,4,4), Mat4Transform.translate(0,0.5f,0));

      material = new Material(new Vec3(0.05f, 0.05f, 0.05f), new Vec3(0.5f, 0.5f, 0.5f), new Vec3(0.7f, 0.7f, 0.7f), 10.0f);
      snowSphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId3);

      material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), 4.0f);
      stoneSphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId4);

      material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.2f, 0.2f, 0.2f), 2.0f);
      carrotSphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId5);

      material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), 4.0f);
      hatSphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId9);

      material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), 51.2f);
      lamppostSphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId6);

    //Cube shaped object used for part of the hat
      mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
      shader = new Shader(gl, "vs_spheres.txt", "fs_spheres.txt");
      modelMatrix = Mat4.multiply(Mat4Transform.scale(4,4,4), Mat4Transform.translate(0,0.5f,0));
      material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), 4.0f);
      hatCube = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId10);


    //Shiny Cube object with translations set instead of creating a hierarchical structuce for one block
      mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
      shader = new Shader(gl, "vs_cube.txt", "fs_cube.txt");
      material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.8f, 0.8f, 0.8f), 90.0f);
      //modelMatrix = Mat4.multiply(Mat4.multiply(Mat4Transform.scale(2.5f,4,2.5f), Mat4Transform.translate(2.5f, 0.5f, 0.0f)), Mat4Transform.rotateAroundY(45f));
      modelMatrix = Mat4.multiply(Mat4Transform.scale(2.5f,4,2.5f), Mat4Transform.translate(2.5f, 0.5f, 0.0f));
      cube = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId7, textureId8);

/*
     //Shiny Sphere shape
    mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    shader = new Shader(gl, "vs_cube.txt", "fs_cube.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4.multiply(Mat4Transform.scale(4f, 4f, 4f), Mat4Transform.translate(1.5f, 0.5f, 0.0f)), Mat4Transform.rotateAroundY(90f));
    //modelMatrix = Mat4.multiply(Mat4Transform.scale(4f, 4f, 4f), Mat4Transform.translate(1.5f, 0.5f, 0.0f));
    sphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId7, textureId8);
*/

  //Snowman
  //*********************************************************************
   //Defining variables for body parts
    float bodySize = 4f;
      float buttonSize = 0.5f;
      float buttonAngle = 18f;
      float headSize = 2.3f;
        float eyeSize = 0.3f;
        float eyeAngle = 15f;
        float noseWidth = 0.25f;
        float noseHeight = 0.25f;
        float noseDepth = 1f;
        float mouthWidth = 0.6f;
        float mouthDepth = 0.6f;
        float mouthHeight = 0.2f;
      float hatBaseDepth = 2f;
      float hatBaseWidth = 2f;
      float hatBaseHeight = 0.6f;
      float hatSideWidth = 0.7f;
      float hatSideHeight = 1.6f;
      float hatSideDepth = 1.3f;
      float hatBackWidth = 2.2f;
      float hatBackHeight = 1.2f;
      float hatBackDepth = 0.7f;
      float hatFrontWidth = 1.7f;
      float hatFrontHeight = 0.5f;
      float hatFrontDepth = 0.7f;

    //All nodes to produce the hierarchical structure of the snowman
    snowmanRoot = new NameNode("snowman structure");

      NameNode body = new NameNode("body");
        bodyTranslation = new TransformNode("bodyTranslation", Mat4Transform.translate(0, 0, 0));
        bodyRotate = new TransformNode("bodyRotate", Mat4Transform.rotateAroundX(0f));
        Mat4 m = new Mat4(1);
        m = Mat4Transform.scale(bodySize, bodySize, bodySize);
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode bodyTransform = new TransformNode("bodyTransform", m);
        ModelNode bodyShape = new ModelNode("bodyShape", snowSphere);

      NameNode button1 = new NameNode("button 1");
        m = new Mat4(1);
        m = Mat4.multiply(m, Mat4Transform.translate(0, (bodySize*0.5f), (bodySize*0.5f)));
        m = Mat4.multiply(m, Mat4Transform.scale(buttonSize, buttonSize, buttonSize));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode button1Transform = new TransformNode("button 1 transform", m);
        ModelNode buttonShape = new ModelNode("b1", stoneSphere);

      NameNode button2 = new NameNode("button 2");
        m = new Mat4(1);
        //Translate
        m = Mat4.multiply(m, Mat4Transform.translate(0, bodySize*0.5f, bodySize*0.5f));
        //Rotate
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0, -bodySize*0.5f));
        m = Mat4.multiply(m, Mat4Transform.rotateAroundX(-buttonAngle));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0, bodySize*0.5f));
        //scale
        m = Mat4.multiply(m, Mat4Transform.scale(buttonSize, buttonSize, buttonSize));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode button2Transform = new TransformNode("button 2 transform", m);
        ModelNode button2Shape = new ModelNode("b2", stoneSphere);

      NameNode button3 = new NameNode("button 3");
        m = new Mat4(1);
        //Translate
        m = Mat4.multiply(m, Mat4Transform.translate(0, bodySize*0.5f, bodySize*0.5f));
        //Rotate
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0, -bodySize*0.5f));
        m = Mat4.multiply(m, Mat4Transform.rotateAroundX(buttonAngle));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0, bodySize*0.5f));
        //Scale
        m = Mat4.multiply(m, Mat4Transform.scale(buttonSize, buttonSize, buttonSize));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode button3Transform = new TransformNode("button 3 transform", m);
        ModelNode button3Shape = new ModelNode("b3", stoneSphere);

      NameNode head = new NameNode("head");
        headRotate = new TransformNode("headRotate", Mat4Transform.rotateAroundZ(0f));
        m = new Mat4(1);
        m = Mat4.multiply(m , Mat4Transform.translate(0, bodySize, 0));
        TransformNode headTranslate = new TransformNode("head Translate", m);

        m = new Mat4(1);
        m = Mat4.multiply(m, Mat4Transform.scale(headSize, headSize, headSize));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode headTransform = new TransformNode("head Transform", m);
        ModelNode headShape = new ModelNode("headshape", snowSphere);

      NameNode leftEye = new NameNode("left eye");
        m = new Mat4(1);
        //Translate
        m = Mat4.multiply(m, Mat4Transform.translate(0, headSize*0.5f, headSize*0.5f));
        //Rotate
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0, -0.05f-headSize*0.5f));
        m = Mat4.multiply(m, Mat4Transform.rotateAroundY(-20));
        m = Mat4.multiply(m, Mat4Transform.rotateAroundX(-12));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0, headSize*0.5f));
        //Scale
        m = Mat4.multiply(m, Mat4Transform.scale(eyeSize, eyeSize, eyeSize));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode leftEyeTransform = new TransformNode("left eye transform", m);
        ModelNode leftEyeShape = new ModelNode("l eye", stoneSphere);

      NameNode rightEye = new NameNode("right eye");
        m = new Mat4(1);
        //Translate
        m = Mat4.multiply(m, Mat4Transform.translate(0, headSize*0.5f, headSize*0.5f));
        //Rotate
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0, -0.05f-headSize*0.5f));
        m = Mat4.multiply(m, Mat4Transform.rotateAroundY(20));
        m = Mat4.multiply(m, Mat4Transform.rotateAroundX(-12));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0, headSize*0.5f));
        //Scale
        m = Mat4.multiply(m, Mat4Transform.scale(eyeSize, eyeSize, eyeSize));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode rightEyeTransform = new TransformNode("right eye transform", m);
        ModelNode rightEyeShape = new ModelNode("r eye", stoneSphere);

      NameNode nose = new NameNode("nose");
        m = new Mat4(1);
        //Translate
        m = Mat4.multiply(m, Mat4Transform.translate(0, headSize*0.5f, headSize*0.5f));
        //Rotate

        //Scale
        m = Mat4.multiply(m, Mat4Transform.scale(noseWidth, noseHeight, noseDepth));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode noseTransform = new TransformNode("nose transform", m);
        ModelNode noseShape = new ModelNode("nose shape", carrotSphere);

      NameNode mouth = new NameNode("mouth");
        m = new Mat4(1);
        //Translate
        m = Mat4.multiply(m, Mat4Transform.translate(0, headSize*0.5f, headSize*0.5f));
        //Rotate
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0, -0.05f-headSize*0.5f));
        m = Mat4.multiply(m, Mat4Transform.rotateAroundX(25));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0, headSize*0.5f));
        //Scale
        m = Mat4.multiply(m, Mat4Transform.scale(mouthWidth, mouthHeight, mouthDepth));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode mouthTransform = new TransformNode("mouth transform", m);
        ModelNode mouthShape = new ModelNode("mouth shape", stoneSphere);

      NameNode hat = new NameNode("hat");
        m = new Mat4(1);
        m = Mat4.multiply(m , Mat4Transform.translate(0, headSize*0.9f, 0));
        TransformNode hatTranslate = new TransformNode("hat Translate", m);
        m = new Mat4(1);
        m = Mat4.multiply(m, Mat4Transform.scale(hatBaseWidth, hatBaseHeight, hatBaseDepth));
        m = Mat4.multiply(m, Mat4Transform.translate(0.0f, 0.5f, 0.0f));
        TransformNode hatTransform = new TransformNode("hat transform", m);
        ModelNode hatShape = new ModelNode("hat shape", hatCube);

      NameNode hatLeft = new NameNode("hat left");
        m = new Mat4(1);
        m = Mat4.multiply(m, Mat4Transform.translate(-hatBaseWidth*0.45f, -hatSideHeight*0.7f, -hatBaseDepth*0.05f));
        m = Mat4.multiply(m, Mat4Transform.scale(hatSideWidth, hatSideHeight, hatSideDepth));
        m = Mat4.multiply(m, Mat4Transform.translate(0.0f, 0.5f, 0.0f));
        TransformNode hatLeftTransform = new TransformNode("hat left transform", m);
        ModelNode hatLeftShape = new ModelNode("hat left shape", hatSphere);

      NameNode hatRight = new NameNode("hat Right");
        m = new Mat4(1);
        m = Mat4.multiply(m, Mat4Transform.translate(hatBaseWidth*0.45f, -hatSideHeight*0.7f, -hatBaseDepth*0.05f));
        m = Mat4.multiply(m, Mat4Transform.scale(hatSideWidth, hatSideHeight, hatSideDepth));
        m = Mat4.multiply(m, Mat4Transform.translate(0.0f, 0.5f, 0.0f));
        TransformNode hatRightTransform = new TransformNode("hat right transform", m);
        ModelNode hatRightShape = new ModelNode("hat right shape", hatSphere);

      NameNode hatBack = new NameNode("hat Back");
        m = new Mat4(1);
        m = Mat4.multiply(m, Mat4Transform.translate(0.0f, -hatBackHeight*0.6f, -hatBaseDepth*0.45f));
        m = Mat4.multiply(m, Mat4Transform.scale(hatBackWidth, hatBackHeight, hatBackDepth));
        m = Mat4.multiply(m, Mat4Transform.translate(0.0f, 0.5f, 0.0f));
        TransformNode hatBackTransform = new TransformNode("hat back transform", m);
        ModelNode hatBackShape = new ModelNode("hat back shape", hatSphere);

      NameNode hatFront = new NameNode("hat Front");
        m = new Mat4(1);
        m = Mat4.multiply(m, Mat4Transform.translate(0.0f, 0.0f, hatBaseDepth*0.45f));
        m = Mat4.multiply(m, Mat4Transform.scale(hatFrontWidth, hatFrontHeight, hatFrontDepth));
        m = Mat4.multiply(m, Mat4Transform.translate(0.0f, 0.5f, 0.0f));
        TransformNode hatFrontTransform = new TransformNode("hat front transform", m);
        ModelNode hatFrontShape = new ModelNode("hat front shape", hatSphere);

    //Snowman hierarchical structure
    snowmanRoot.addChild(bodyTranslation);
      bodyTranslation.addChild(bodyRotate);
        bodyRotate.addChild(body);
          body.addChild(bodyTransform);
              bodyTransform.addChild(bodyShape);
          body.addChild(button1);
              button1.addChild(button1Transform);
              button1Transform.addChild(buttonShape);
          body.addChild(button2);
              button2.addChild(button2Transform);
              button2Transform.addChild(button2Shape);
          body.addChild(button3);
              button3.addChild(button3Transform);
              button3Transform.addChild(button3Shape);
          body.addChild(headTranslate);
              headTranslate.addChild(headRotate);
              headRotate.addChild(head);
                head.addChild(headTransform);
                    headTransform.addChild(headShape);
                  head.addChild(leftEye);
                    leftEye.addChild(leftEyeTransform);
                    leftEyeTransform.addChild(leftEyeShape);
                  head.addChild(rightEye);
                    rightEye.addChild(rightEyeTransform);
                    rightEyeTransform.addChild(rightEyeShape);
                  head.addChild(nose);
                    nose.addChild(noseTransform);
                    noseTransform.addChild(noseShape);
                  head.addChild(mouth);
                    mouth.addChild(mouthTransform);
                    mouthTransform.addChild(mouthShape);
                  head.addChild(hatTranslate);
                    hatTranslate.addChild(hat);
                      hat.addChild(hatTransform);
                        hatTransform.addChild(hatShape);
                      hat.addChild(hatLeft);
                        hatLeft.addChild(hatLeftTransform);
                          hatLeftTransform.addChild(hatLeftShape);
                      hat.addChild(hatRight);
                        hatRight.addChild(hatRightTransform);
                          hatRightTransform.addChild(hatRightShape);
                      hat.addChild(hatBack);
                        hatBack.addChild(hatBackTransform);
                          hatBackTransform.addChild(hatBackShape);
                      hat.addChild(hatFront);
                        hatFront.addChild(hatFrontTransform);
                          hatFrontTransform.addChild(hatFrontShape);
    snowmanRoot.update();

  //*********************************************************************************************

  //Lamp
  //*********************************************************************************************
    //Defining variables for lamp
      float lampTranslateX = -7f;
      float lampTranslateZ = -3f;
      float lampBodyHeight = 12f;
      float lampBodyWidth = 0.5f;
      float lampBodyDepth = 0.5f;
      float lampHeadHeight = 0.5f;
      float lampHeadWidth = 3.5f;
      float lampHeadDepth = 0.5f;

    lampRoot = new NameNode("Lamp structure");

      NameNode lampBody = new NameNode("lamp body");
        TransformNode lampBodyTranslation = new TransformNode("lampBodyTranslation", Mat4Transform.translate(lampTranslateX, 0.0f, lampTranslateZ));
        m = new Mat4(1);
        m = Mat4Transform.scale(lampBodyWidth, lampBodyHeight, lampBodyDepth);
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode lampBodyTransform = new TransformNode("lampBodyTransform", m);
        ModelNode lampBodyShape = new ModelNode("lamp bodyshape", lamppostSphere);

      NameNode lampHead = new NameNode("lamp head");
        lampHeadRotate = new TransformNode("lampHeadRotate", Mat4Transform.rotateAroundY(0f));
        m = new Mat4(1);
        m = Mat4.multiply(m , Mat4Transform.translate(0, lampBodyHeight*0.5f, 0));
        TransformNode lampHeadTranslate = new TransformNode("lamp head Translate", m);
        new Mat4(1);
        m = Mat4.multiply(m, Mat4Transform.rotateAroundZ(-15f));
        m = Mat4.multiply(m, Mat4Transform.rotateAroundY(-15f));
        m = Mat4.multiply(m, Mat4Transform.scale(lampHeadWidth, lampHeadHeight, lampHeadDepth));
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode lampHeadTransform = new TransformNode("lamp head Transform", m);
        ModelNode lampHeadShape = new ModelNode("lamp headshape", lamppostSphere);

    lampRoot.addChild(lampBodyTranslation);
      lampBodyTranslation.addChild(lampBody);
        lampBody.addChild(lampBodyTransform);
        lampBodyTransform.addChild(lampBodyShape);
      lampBody.addChild(lampHeadTranslate);
          lampHeadTranslate.addChild(lampHeadRotate);
          lampHeadRotate.addChild(lampHead);
            lampHead.addChild(lampHeadTransform);
              lampHeadTransform.addChild(lampHeadShape);
    lampRoot.update();

  //Initialise spotlight position and direction
    spotlightX = lampTranslateX+lampHeadWidth*0.4f;
    spotlightY = lampBodyHeight*0.975f;
    spotlightZ = lampTranslateZ+lampHeadWidth*0.1f;
    spotDirectionX = 0.7f;
    spotDirectionY = -0.55f;
    spotDirectionZ = 0.2f;

    light.setSpotLightPosition(spotlightX, spotlightY, spotlightZ);
    light.setSpotLightDirection(spotDirectionX, spotDirectionY, spotDirectionZ);

  //*********************************************************************************************

    //twoBranchRoot.print(0, false);
    //System.exit(0);
  }

  private void render(GL3 gl) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    setLights();
    light.render(gl);

    floor.render(gl);

    updateWall();
    wall.setModelMatrix(getMforWall());
    wall.render(gl);

    if(rock) updateRock();
    if(roll) updateRoll();
    if(slide) updateSlide();
    snowmanRoot.draw(gl);
    lampRoot.draw(gl);
    cube.render(gl);

  }


  private void setLights(){
  //Turn  directional light on/off depending on whether button on frame is pressed
    if(!lights){
      Material ADS = new Material(new Vec3(0f, 0f, 0f), new Vec3(0.0f, 0.0f, 0.0f), new Vec3(0.0f, 0.0f, 0.0f), 0.0f);
      light.setDirLightADS(ADS);
    }else{ //else turn directional light on
      Material ADS = new Material(new Vec3(0.2f, 0.2f, 0.2f), new Vec3(0.4f, 0.4f, 0.4f), new Vec3(0.4f, 0.4f, 0.4f), 0.0f);
      light.setDirLightADS(ADS);
    }
    //Turn spotlight on/off depending on whether button on frame is pressed
    if(!spotlight){
      Material ADS = new Material(new Vec3(0f, 0f, 0f), new Vec3(0.0f, 0.0f, 0.0f), new Vec3(0.0f, 0.0f, 0.0f), 0.0f);
      light.setSpotLightADS(ADS);
    }else{
      Material ADS = new Material(new Vec3(0f, 0f, 0f), new Vec3(1.0f, 1.0f, 1.0f), new Vec3(1.0f, 1.0f, 1.0f), 0.0f);
      light.setSpotLightADS(ADS);
    }

    /* ***************************************************************************

      I could not figure out best to make the light rotate in a circle on the lamppost as the
      light position is a vec3 whereas transforms for the other objects use modelMatrix
      so I simply made the light swing back and forth instead for the same effect.

    */ // *********************************************************************************

    //Rotate light back and forth whe selected
    if(spotRotate){
      light.setSpotLightDirection(getRotatingSpotLightDirection());
    }else{
      light.setSpotLightPosition(spotlightX, spotlightY, spotlightZ);
      light.setSpotLightDirection(spotDirectionX, spotDirectionY, spotDirectionZ);
    }
  }

  private Vec3 getRotatingSpotLightDirection(){
    double elapsedTime = getSeconds()-startTime;
    float x =  0.7f;
    float y = -0.52f;
    float z = 0.2f + 0.25f*(float)(Math.cos(Math.toRadians(elapsedTime*35)));
    return new Vec3(x, y, z);
  }

   //function to update the offset applied to the second texture of the wall
  private void updateWall(){
    double elapsedTime = getSeconds()-startTime;
    double t = elapsedTime*0.1;
    float offsetX = -(float)(t - Math.floor(t));
    float offsetY = (float)(t - Math.floor(t));
    light.setOffset(offsetX, offsetY);
  }

  //Function to make the body rock side to side along the X
  private void updateRock(){
    double elapsedTime = getSeconds()-startTime;
    float rotateAngle = 35f*(float)Math.sin(elapsedTime);
    bodyRotate.setTransform(Mat4Transform.rotateAroundX(rotateAngle));  //Swap commented out with below line to rotate along Z instead
    //bodyRotate.setTransform(Mat4Transform.rotateAroundZ(rotateAngle));
    bodyRotate.update();
  }

  //Function to make the head roll around the top of the body along the Z axis
  private void updateRoll(){
    double elapsedTime = getSeconds()-startTime;
    float rotateAngle = 30f*(float)Math.sin(elapsedTime);
    Mat4 m = new Mat4(1);
    m = Mat4.multiply(m , Mat4Transform.translate(0, -1.35f, 0));
    m = Mat4.multiply(m, Mat4Transform.rotateAroundZ(rotateAngle));
    m = Mat4.multiply(m , Mat4Transform.translate(0, 1.35f, 0));
    headRotate.setTransform(m);
    headRotate.update();
  }

  //Function to make the snowman slide side to side along the X axis
  private void updateSlide(){
    double elapsedTime = getSeconds()-startTime;
    float xPosition = 3f*(float)Math.sin(elapsedTime);
    float zPosition = 0f; //3f*(float)Math.cos(elapsedTime); //Use to make the snowman slide around in a circle
    bodyTranslation.setTransform(Mat4Transform.translate(xPosition,0,zPosition));
    bodyTranslation.update();
  }

  //Set model matrix for wall which translates it from the floor position.
  private Mat4 getMforWall() {
    float width = 30f;
    float size = 16f;
    Mat4 modelMatrix = new Mat4(1);
    modelMatrix = Mat4.multiply(Mat4Transform.scale(width,1f,size), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(0, size*0.5f, -size*0.5f), modelMatrix);
    return modelMatrix;
  }

  // ***************************************************
  /* TIME
   */

  private double startTime;

  private double getSeconds() {
    return System.currentTimeMillis()/1000.0;
  }

}
