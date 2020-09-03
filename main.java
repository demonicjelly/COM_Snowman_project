/* I declare that this code is my own work */
/* Author Dominic Jolley - djolley1@sheffield.ac.uk

  Base structure taken from M03_GLEventListener


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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

public class main extends JFrame implements ActionListener {

  private static final int WIDTH = 1024;
  private static final int HEIGHT = 768;
  private static final Dimension dimension = new Dimension(WIDTH, HEIGHT);
  private GLCanvas canvas;
  private main_GLEventListener glEventListener;
  private final FPSAnimator animator;
  private Camera camera;

  public static void main(String[] args) {
    main b1 = new main("main");
    b1.getContentPane().setPreferredSize(dimension);
    b1.pack();
    b1.setVisible(true);
  }

  public main(String textForTitleBar) {
    super(textForTitleBar);
    GLCapabilities glcapabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));
    canvas = new GLCanvas(glcapabilities);
    camera = new Camera(Camera.DEFAULT_POSITION, Camera.DEFAULT_TARGET, Camera.DEFAULT_UP);
    glEventListener = new main_GLEventListener(camera);
    canvas.addGLEventListener(glEventListener);
    canvas.addMouseMotionListener(new MyMouseInput(camera));
    canvas.addKeyListener(new MyKeyboardInput(camera));
    getContentPane().add(canvas, BorderLayout.CENTER);

    JMenuBar menuBar=new JMenuBar();
    this.setJMenuBar(menuBar);
      JMenu fileMenu = new JMenu("File");
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(this);
        fileMenu.add(quitItem);
    menuBar.add(fileMenu);

    JPanel p = new JPanel();
      JButton b = new JButton("camera X");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("camera Z");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Lights on/off");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Spotlight on/off");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Spotlight Rotate");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Rock");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Roll");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Slide");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Slide, Rock and Roll");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("reset");
      b.addActionListener(this);
      p.add(b);
    this.add(p, BorderLayout.SOUTH);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        animator.stop();
        remove(canvas);
        dispose();
        System.exit(0);
      }
    });
    animator = new FPSAnimator(canvas, 60);
    animator.start();
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("camera X")) {
      camera.setCamera(Camera.CameraType.X);
      canvas.requestFocusInWindow();
    }
    else if (e.getActionCommand().equalsIgnoreCase("camera Z")) {
      camera.setCamera(Camera.CameraType.Z);
      canvas.requestFocusInWindow();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Lights on/off")) {
      glEventListener.lightsOnOff();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Spotlight on/off")) {
      glEventListener.spotlightOnOff();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Spotlight Rotate")) {
      glEventListener.spotlightRotate();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Rock")) {
      glEventListener.resetSnowman();
      glEventListener.rockAnimation();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Roll")) {
      glEventListener.resetSnowman();
      glEventListener.rollAnimation();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Slide")) {
      glEventListener.resetSnowman();
      glEventListener.slideAnimation();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Slide, Rock and Roll")) {
      glEventListener.resetSnowman();
      glEventListener.rrsAnimation();
    }
    else if (e.getActionCommand().equalsIgnoreCase("reset")) {
      glEventListener.resetSnowman();
    }
    else if(e.getActionCommand().equalsIgnoreCase("quit"))
      System.exit(0);
  }

}

class MyKeyboardInput extends KeyAdapter  {
  private Camera camera;

  public MyKeyboardInput(Camera camera) {
    this.camera = camera;
  }

  public void keyPressed(KeyEvent e) {
    Camera.Movement m = Camera.Movement.NO_MOVEMENT;
    switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT:  m = Camera.Movement.LEFT;  break;
      case KeyEvent.VK_RIGHT: m = Camera.Movement.RIGHT; break;
      case KeyEvent.VK_UP:    m = Camera.Movement.UP;    break;
      case KeyEvent.VK_DOWN:  m = Camera.Movement.DOWN;  break;
      case KeyEvent.VK_A:  m = Camera.Movement.FORWARD;  break;
      case KeyEvent.VK_Z:  m = Camera.Movement.BACK;  break;
    }
    camera.keyboardInput(m);
  }
}

class MyMouseInput extends MouseMotionAdapter {
  private Point lastpoint;
  private Camera camera;

  public MyMouseInput(Camera camera) {
    this.camera = camera;
  }

    /**
   * mouse is used to control camera position
   *
   * @param e  instance of MouseEvent
   */
  public void mouseDragged(MouseEvent e) {
    Point ms = e.getPoint();
    float sensitivity = 0.001f;
    float dx=(float) (ms.x-lastpoint.x)*sensitivity;
    float dy=(float) (ms.y-lastpoint.y)*sensitivity;
    //System.out.println("dy,dy: "+dx+","+dy);
    if (e.getModifiers()==MouseEvent.BUTTON1_MASK)
      camera.updateYawPitch(dx, -dy);
    lastpoint = ms;
  }

  /**
   * mouse is used to control camera position
   *
   * @param e  instance of MouseEvent
   */
  public void mouseMoved(MouseEvent e) {
    lastpoint = e.getPoint();
  }
}
