/* I declare that this code is my own work */
/* Author Dominic Jolley - djolley1@sheffield.ac.uk

  Base of this class taken from tutorials and has been changed to add in the
  spotlight and directional light

*/

import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;

public class Light {

  private Vec3 dirLightDirection;
  private Material dirLightADS;

  private Vec3 spotLightPosition;
  private Vec3 spotLightDirection;
  private Material spotLightADS;
  private float spotLightConstant;
  private float spotLightLinear;
  private float spotLightQuadratic;
  private float spotLightCutOff;
  private float spotLightOuterCutOff;

  private float offsetX;
  private float offsetY;

  private Material material;
  private Vec3 position;
  private Mat4 model;
  private Shader shader;
  private Camera camera;

  public Light(GL3 gl) {
    material = new Material();
    material.setAmbient(0.3f, 0.3f, 0.3f);
    material.setDiffuse(0.7f, 0.7f, 0.7f);
    material.setSpecular(1.0f, 1.0f, 1.0f);

    //DirLight variables
    dirLightDirection = new Vec3(-0.4f, -0.5f, -0.4f);;
    dirLightADS = new Material();
    dirLightADS.setAmbient(0.3f, 0.3f, 0.3f);
    dirLightADS.setDiffuse(0.7f, 0.8f, 0.8f);
    dirLightADS.setSpecular(0.6f, 0.6f, 0.6f);

    //SpotLight variables
    spotLightPosition = new Vec3(3f, 3f, -3.0f);
    spotLightDirection = new Vec3(0.7f, -0.5f, 0.0f);
    spotLightADS = new Material();
    spotLightADS.setAmbient(0.0f, 0.0f, 0.0f);
    spotLightADS.setDiffuse(1.0f, 1.0f, 1.0f);
    spotLightADS.setSpecular(1.0f, 1.0f, 1.0f);
    spotLightConstant = 1.0f;
    spotLightLinear = 0.027f;
    spotLightQuadratic = 0.0028f;
    spotLightCutOff = (float)Math.cos(Math.toRadians(5));
    spotLightOuterCutOff = (float)Math.cos(Math.toRadians(7.5));

    position = spotLightPosition;
    model = new Mat4(1);
    shader = new Shader(gl, "vs_light_01.txt", "fs_light_01.txt");
    fillBuffers(gl);
  }

  //Set and get offset

  public void setOffset(float x, float y){
    offsetX = x;
    offsetY = y;
  }

  public float getOffsetX(){
    return offsetX;
  }
  public float getOffsetY(){
    return offsetY;
  }

  //Set for Dirlight
  public void setDirLightADS(Material m) {
    dirLightADS = m;
  }

  //Get for dirLight
  //***************************************************
  public Vec3 getDirLightDirection() {
    return dirLightDirection;
  }

  public Material getDirLightADS() {
    return dirLightADS;
  }

  //***************************************************************
  //Set for Spotlight
  public void setSpotLightPosition(Vec3 v) {
    spotLightPosition.x = v.x;
    spotLightPosition.y = v.y;
    spotLightPosition.z = v.z;
  }

  public void setSpotLightPosition(float x, float y, float z) {
    spotLightPosition.x = x;
    spotLightPosition.y = y;
    spotLightPosition.z = z;
  }

  public void setSpotLightDirection(Vec3 v) {
    spotLightDirection.x = v.x;
    spotLightDirection.y = v.y;
    spotLightDirection.z = v.z;
  }

  public void setSpotLightDirection(float x, float y, float z) {
    spotLightDirection.x = x;
    spotLightDirection.y = y;
    spotLightDirection.z = z;
  }

  public void setSpotLightADS(Material m) {
    spotLightADS = m;
  }

  //Get for spotlight
  public Vec3 getSpotLightPosition() {
    return spotLightPosition;
  }

  public Vec3 getSpotLightDirection() {
    return spotLightDirection;
  }

  public Material getSpotLightADS() {
    return spotLightADS;
  }

  public float getSpotLightConstant(){
    return spotLightConstant;
  }

  public float getSpotLightLinear(){
    return spotLightLinear;
  }

  public float getSpotLightQuadratic(){
    return spotLightQuadratic;
  }

  public float getSpotLightCutOff(){
    return spotLightCutOff;
  }

  public float getSpotLightOuterCutOff(){
    return spotLightOuterCutOff;
  }

//******************************************************************

  public void setPosition(Vec3 v) {
    position.x = v.x;
    position.y = v.y;
    position.z = v.z;
  }

  public void setPosition(float x, float y, float z) {
    position.x = x;
    position.y = y;
    position.z = z;
  }

  public Vec3 getPosition() {
    return position;
  }

  public void setDirection(Vec3 v) {
    position.x = v.x;
    position.y = v.y;
    position.z = v.z;
  }

  public void setDirection(float x, float y, float z) {
    position.x = x;
    position.y = y;
    position.z = z;
  }

  public void setMaterial(Material m) {
    material = m;
  }

  public Material getMaterial() {
    return material;
  }

  public void setCamera(Camera camera) {
    this.camera = camera;
  }

  public void render(GL3 gl) {
    Mat4 model = new Mat4(1);
    model = Mat4.multiply(Mat4Transform.scale(0.3f,0.3f,0.3f), model);
    model = Mat4.multiply(Mat4Transform.translate(position), model);

    Mat4 mvpMatrix = Mat4.multiply(camera.getPerspectiveMatrix(), Mat4.multiply(camera.getViewMatrix(), model));

    shader.use(gl);
    shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

    gl.glBindVertexArray(vertexArrayId[0]);
    gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
    gl.glBindVertexArray(0);
  }

  public void dispose(GL3 gl) {
    gl.glDeleteBuffers(1, vertexBufferId, 0);
    gl.glDeleteVertexArrays(1, vertexArrayId, 0);
    gl.glDeleteBuffers(1, elementBufferId, 0);
  }

    // ***************************************************
  /* THE DATA
   */
  // anticlockwise/counterclockwise ordering

    private float[] vertices = new float[] {  // x,y,z
      -0.5f, -0.5f, -0.5f,  // 0
      -0.5f, -0.5f,  0.5f,  // 1
      -0.5f,  0.5f, -0.5f,  // 2
      -0.5f,  0.5f,  0.5f,  // 3
       0.5f, -0.5f, -0.5f,  // 4
       0.5f, -0.5f,  0.5f,  // 5
       0.5f,  0.5f, -0.5f,  // 6
       0.5f,  0.5f,  0.5f   // 7
     };

    private int[] indices =  new int[] {
      0,1,3, // x -ve
      3,2,0, // x -ve
      4,6,7, // x +ve
      7,5,4, // x +ve
      1,5,7, // z +ve
      7,3,1, // z +ve
      6,4,0, // z -ve
      0,2,6, // z -ve
      0,4,5, // y -ve
      5,1,0, // y -ve
      2,3,7, // y +ve
      7,6,2  // y +ve
    };

  private int vertexStride = 3;
  private int vertexXYZFloats = 3;

  // ***************************************************
  /* THE LIGHT BUFFERS
   */

  private int[] vertexBufferId = new int[1];
  private int[] vertexArrayId = new int[1];
  private int[] elementBufferId = new int[1];

  private void fillBuffers(GL3 gl) {
    gl.glGenVertexArrays(1, vertexArrayId, 0);
    gl.glBindVertexArray(vertexArrayId[0]);
    gl.glGenBuffers(1, vertexBufferId, 0);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId[0]);
    FloatBuffer fb = Buffers.newDirectFloatBuffer(vertices);

    gl.glBufferData(GL.GL_ARRAY_BUFFER, Float.BYTES * vertices.length, fb, GL.GL_STATIC_DRAW);

    int stride = vertexStride;
    int numXYZFloats = vertexXYZFloats;
    int offset = 0;
    gl.glVertexAttribPointer(0, numXYZFloats, GL.GL_FLOAT, false, stride*Float.BYTES, offset);
    gl.glEnableVertexAttribArray(0);

    gl.glGenBuffers(1, elementBufferId, 0);
    IntBuffer ib = Buffers.newDirectIntBuffer(indices);
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBufferId[0]);
    gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indices.length, ib, GL.GL_STATIC_DRAW);
    gl.glBindVertexArray(0);
  }

}
