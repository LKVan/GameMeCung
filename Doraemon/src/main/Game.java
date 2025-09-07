package main;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL2.*;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class Game extends GLJPanel implements GLEventListener {
	// Define constants for the top-level container
	private static String TITLE = "JOGL 2.0 Setup (GLJPanel)"; // window's title
	private static final int PANEL_WIDTH = 640; // width of the drawable
	private static final int PANEL_HEIGHT = 960; // height of the drawable
	private static final int FPS = 60; // animator's target frames per second

	private GLU glu;
	private GLUT glut;
	private GLUquadric quad;
	private float speed = 0.0f;
	private float cakeSpeed = 0.0f;
	private float charMapX = 60.0f;
	private float charMapZ = 20.0f;
	private float charMapY = 11.0f;
	private float endMapX = 0.0f;
	private float endMapZ = 0.0f;
	private float endMapY = 11.0f;
	float rotationY = 0.0f; 

	private boolean movingForward = false;
	private boolean movingBackward = false;
	private boolean movingLeft = false;
	private boolean movingRight = false;
	private boolean isWalking = false;
	private int score = 0;

	private Texture leafTexture,grassTexture,trunkTexture;

	int[][] map = { 
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, 
			{ 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1 },
			{ 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1 }, 
			{ 1, 0, 0, 0, 1, 1, 1, 0, 1, 0, 1 },
			{ 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1 }, 
			{ 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1 },
			{ 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1 }, 
			{ 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, 
			{ 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1 },
			{ 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1 }, 
			{ 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1 },
			{ 1, 0, 1, 0, 1, 1, 1, 0, 0, 0, 1 }, 
			{ 1, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1 },
			{ 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1 }, 
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },

	};

	float roll = 0.0f, pitch = 0.0f, yaw = 0.0f;
	float camX = 60.0f;
	float camZ = 20.0f;
	float camY = 60.0f;
	
	private TextRenderer textRenderer;
	private long startTime;
	private int countdownSeconds = 60;
	private boolean gameOver = false;

	public static void main(String[] args) {
		System.setProperty("java.library.path", "lib");
		// Run the GUI codes in the event-dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create the OpenGL rendering canvas
				GLJPanel canvas = new Game();
				canvas.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));

				// Create a animator that drives canvas' display() at the specified FPS.
				final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

				// Create the top-level container
				final JFrame frame = new JFrame(); // Swing's JFrame or AWT's Frame
				frame.getContentPane().add(canvas);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						// Use a dedicate thread to run the stop() to ensure that the
						// animator stops before program exits.
						new Thread() {
							@Override
							public void run() {
								if (animator.isStarted())
									animator.stop();
								System.exit(0);
							}
						}.start();
					}
				});
				frame.setTitle(TITLE);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				animator.start(); // start the animation loop
			}
		});
	}

	public Game() {
		this.setPreferredSize(new Dimension(640, 960));
		this.addKeyListener(new KeyHandler());
		this.addGLEventListener(this);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL graphics context
		glu = new GLU(); // get GL Utilities
		glut = new GLUT();
		quad = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(quad, GLU.GLU_FILL);
		glu.gluQuadricNormals(quad, GLU.GLU_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		gl.glShadeModel(GL_SMOOTH);
		glu.gluQuadricTexture(quad, true);

		try {
			leafTexture = TextureIO.newTexture(new File("src\\textures\\flower.jpg"), true);
			grassTexture = TextureIO.newTexture(new File("src\\textures\\grass.jpg"), true);
			grassTexture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			grassTexture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
			trunkTexture = TextureIO.newTexture(new File("src\\textures\\trunk.jpg"), true);
			trunkTexture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			trunkTexture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Bật Lighting
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glShadeModel(GL2.GL_SMOOTH);

		// Ánh sáng môi trường
		float[] ambientLight = { 0.2f, 0.2f, 0.2f, 1.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambientLight, 0);

		// Ánh sáng khuếch tán (diffuse)
		float[] diffuseLight = { 0.8f, 0.8f, 0.8f, 1.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuseLight, 0);

		// Ánh sáng phản chiếu (specular)
		float[] specularLight = { 0.5f, 0.5f, 0.5f, 1.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specularLight, 0);

		// Vị trí ánh sáng
		float[] lightPos = { 0.0f, 10.0f, 10.0f, 1.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);

		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);

		float[] matSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpecular, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 50.0f); // độ bóng
		
		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		
		textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24), true, false);
		startTime = System.currentTimeMillis();
		gameOver = false;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context

		if (height == 0)
			height = 1;
		float aspect = (float) width / height;

		gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0, aspect, 10.0, 500.0);

		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity(); // reset
		randomCakeLocation();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		// Camera
		gl.glLoadIdentity();
		glu.gluLookAt(camX, camY, camZ - 40.0f, charMapX, 0.0f, charMapZ, 0.0f, 0.0f, 1.0f);

		for (int z = 0; z < map.length; z++) {
			for (int x = 0; x < map[0].length; x++) {
				if (map[z][x] == 1) {
					gl.glPushMatrix();
					gl.glTranslatef(x * 10, 0.0f, z * 10);
					gl.glScalef(1.0f, 1.0f, 1.0f);
					drawTree(gl);
					gl.glPopMatrix();
				}
			}
		}
		gl.glPushMatrix();
		gl.glTranslatef(charMapX, charMapY, charMapZ);
		gl.glTranslatef(pitch, 0.0f, 0.0f);
		gl.glTranslatef(0.0f, 0.0f, yaw);
		gl.glScalef(0.15f, 0.15f, 0.15f);
		gl.glRotatef(rotationY, 0.0f, 1.0f, 0.0f);
		drawCharacter(gl);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslatef(endMapX, endMapY, endMapZ);
		gl.glScalef(0.2f, 0.2f, 0.2f);
		gl.glRotatef(cakeSpeed, 1.0f, 1.0f, 1.0f);
		drawCake(gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glTranslatef(map[0].length * 5.0f - 5.0f, -3.0f, map.length * 5.0f - 5.0f);
		gl.glScalef(map[0].length * 8.0f, 5.0f, map.length * 8.0f);
		grassTexture.enable(gl);
		grassTexture.bind(gl);
		gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0f, 0f); gl.glVertex3f(-1f, 0f, -1f);
        gl.glTexCoord2f(4f, 0f); gl.glVertex3f( 1f, 0f, -1f);
        gl.glTexCoord2f(4f, 4f); gl.glVertex3f( 1f, 0f,  1f);
        gl.glTexCoord2f(0f, 4f); gl.glVertex3f(-1f, 0f,  1f);
        gl.glEnd();
		grassTexture.disable(gl);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; 
		int remaining = countdownSeconds - (int) elapsedTime;

		textRenderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        textRenderer.setColor(1f, 0f, 0f, 1f);
        textRenderer.draw("Time Left: " + remaining, 30, drawable.getSurfaceHeight() - 30);
        textRenderer.endRendering();
        if (remaining <= 0) {
            gameOver = true;
        }
        gl.glPopMatrix();
        
        gl.glPushMatrix();
		textRenderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        textRenderer.setColor(1f, 0f, 0f, 1f);
        textRenderer.draw("Score: " + score + "/3", drawable.getSurfaceWidth() - 150, drawable.getSurfaceHeight() - 30);
        textRenderer.endRendering();
        gl.glPopMatrix();

		checkEnd();
		

	}

	public void drawTree(GL2 gl) {
		gl.glPushMatrix();
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		gl.glColor3f(0.65f, 0.50f, 0.39f);
		trunkTexture.enable(gl);
		trunkTexture.bind(gl);
		glu.gluCylinder(quad, 4.0, 3.0, 11.0, 16, 16);
		trunkTexture.disable(gl);
		
		gl.glTranslatef(0.0f, 0.0f, 12.0f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		leafTexture.enable(gl);
		leafTexture.bind(gl);
		glu.gluSphere(quad, 5.5f, 20, 20);
		leafTexture.disable(gl);
		gl.glPopMatrix();

	}

	public void drawCharacter(GL2 gl) {
		gl.glTranslated(0.0f, 0.0f, -10.0f);
		drawHead(gl);
		gl.glTranslated(0.0f, -29.0f, 0.0f);
		drawBody(gl);
		speed += 0.15f;
	}

	public void drawPartialSphere(GL2 gl, float radius, int slices, int stacks, float phiMaxDeg) {
		float phiMax = (float) Math.toRadians(phiMaxDeg);
		float dPhi = phiMax / stacks;
		float dTheta = (float) (2 * Math.PI / slices);

		for (int i = 0; i < stacks; i++) {
			float phi0 = i * dPhi;
			float phi1 = (i + 1) * dPhi;

			gl.glBegin(GL_QUAD_STRIP);
			for (int j = 0; j <= slices; j++) {
				float theta = j * dTheta;

				float x0 = radius * (float) (Math.sin(phi0) * Math.cos(theta));
				float y0 = radius * (float) (Math.sin(phi0) * Math.sin(theta));
				float z0 = radius * (float) (Math.cos(phi0));

				float x1 = radius * (float) (Math.sin(phi1) * Math.cos(theta));
				float y1 = radius * (float) (Math.sin(phi1) * Math.sin(theta));
				float z1 = radius * (float) (Math.cos(phi1));

				gl.glNormal3f(x0 / radius, y0 / radius, z0 / radius);
				gl.glVertex3f(x0, y0, z0);

				gl.glNormal3f(x1 / radius, y1 / radius, z1 / radius);
				gl.glVertex3f(x1, y1, z1);
			}
			gl.glEnd();
		}
	}

	public void drawHead(GL2 gl) {
		// đầu
		gl.glColor3f(0.0f, 0.0f, 1.0f);
		glu.gluSphere(quad, 30.0f, 50, 50);

		// mặt
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, -3.0f, 6.0f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		drawPartialSphere(gl, 26.0f, 50, 25, 90.0f);
		gl.glPopMatrix();

		// mũi
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 2.0f, 33.0f);
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		glu.gluSphere(quad, 3.5f, 20, 20);
		gl.glPopMatrix();

		// mắt  trái
		gl.glPushMatrix();
		gl.glTranslatef(-3.6f, 10.0f, 27.4f);
		gl.glRotated(-30.0f, 1.0f, 0.0f, 0.0f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glScaled(1.0f, 1.5f, 0.5f);
		drawPartialSphere(gl, 4.0f, 50, 25, 90.0f);

		gl.glTranslatef(0.4f, -3.0f, 3.0f);
		gl.glColor3f(0.0f, 0.0f, 0.0f);
		glu.gluSphere(quad, 1.5f, 10, 10);
		gl.glTranslatef(0.0f, 0.0f, 1.0f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		glu.gluSphere(quad, 0.6f, 10, 10);
		gl.glPopMatrix();

		// mắt  phải
		gl.glPushMatrix();
		gl.glTranslatef(3.6f, 10.0f, 27.4f);
		gl.glRotated(-30.0f, 1.0f, 0.0f, 0.0f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glScaled(1.0f, 1.5f, 0.5f);
		drawPartialSphere(gl, 4.0f, 50, 25, 90.0f);

		gl.glTranslatef(0.0f, -3.0f, 3.0f);
		gl.glColor3f(0.0f, 0.0f, 0.0f);
		glu.gluSphere(quad, 1.5f, 10, 10);
		gl.glTranslatef(0.0f, 0.0f, 1.0f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		glu.gluSphere(quad, 0.6f, 10, 10);
		gl.glPopMatrix();
		// Miệng
		gl.glPushMatrix();
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glTranslatef(0.0f, -16.0f, 25.5f);
		gl.glRotated(37.0f, 1.0f, 0.0f, 0.0f);
		gl.glScalef(0.6f, 0.6f, 0.40f);
		drawPartialSphere(gl, 8.0f, 50, 25, 90.0f);
		gl.glPopMatrix();

		// mép
		gl.glColor3f(0.0f, 0.0f, 0.0f);
		gl.glLineWidth(0.5f);
		gl.glBegin(GL_LINES);
		gl.glVertex3f(0.0f, -1.5f, 32.0f);
		gl.glVertex3f(0.0f, -9.5f, 32.0f);
		gl.glEnd();
		// râu
		gl.glColor3f(0.0f, 0.0f, 0.0f);
		gl.glLineWidth(2.0f);
		gl.glBegin(GL_LINES);

		// râu trái
		gl.glVertex3f(-10.0f, 0.0f, 30.0f);
		gl.glVertex3f(-20.0f, 3.0f, 30.0f);
		gl.glVertex3f(-10.0f, -2.0f, 30.0f);
		gl.glVertex3f(-20.0f, -2.0f, 30.0f);
		gl.glVertex3f(-10.0f, -4.0f, 30.0f);
		gl.glVertex3f(-20.0f, -7.0f, 30.0f);

		// râu phải
		gl.glVertex3f(10.0f, 0.0f, 30.0f);
		gl.glVertex3f(20.0f, 3.0f, 30.0f);
		gl.glVertex3f(10.0f, -2.0f, 30.0f);
		gl.glVertex3f(20.0f, -2.0f, 30.0f);
		gl.glVertex3f(10.0f, -4.0f, 30.0f);
		gl.glVertex3f(20.0f, -7.0f, 30.0f);
		gl.glEnd();
	}

	public void drawBody(GL2 gl) {
		// vòng cổ
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glPushMatrix();
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		glu.gluDisk(quad, 0.0, 18.0, 32, 1);
		glu.gluCylinder(quad, 18.0, 18.0, 3.0, 32, 32);
		gl.glTranslatef(0.0f, 0.0f, 3.0f);
		glu.gluDisk(quad, 0.0, 18.0, 32, 1);
		gl.glPopMatrix();

		// chuông
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glTranslated(0.0f, 0.0f, 18.0f);
		glu.gluSphere(quad, 4.0f, 50, 50);
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glPushMatrix();
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		glu.gluDisk(quad, 0.0, 5.0, 32, 1);
		glu.gluCylinder(quad, 5.0, 5.0, 1.0, 32, 32);
		gl.glTranslatef(0.0f, 0.0f, 1.0f);
		glu.gluDisk(quad, 0.0, 5.0, 32, 1);
		gl.glPopMatrix();
		gl.glTranslatef(0.0f, -1.0f, 3.0f);
		gl.glColor3f(0.0f, 0.0f, 0.0f);
		glu.gluSphere(quad, 1.3f, 10, 10);

		// tay phải
		gl.glPushMatrix();
		if (isWalking) {
		gl.glRotatef((float) Math.sin(speed) * 10, 0.0f, 0.0f, 1.0f);
		}
		gl.glTranslatef(-30.0f, -5.0f, -20.0f);
		gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
		gl.glColor3f(0.0f, 0.0f, 1.0f);
		glu.gluCylinder(quad, 4.0, 6.0, 14.0, 32, 32);
		// bàn tay phải
		gl.glTranslatef(0.0f, 0.0f, -2.0f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		glu.gluSphere(quad, 5.0f, 20, 20);
		gl.glPopMatrix();

		// tay trái
		gl.glPushMatrix();
		if (isWalking) {
		gl.glRotatef(-(float) Math.sin(speed) * 10, 0.0f, 0.0f, 1.0f);
		}
		gl.glTranslatef(30.0f, -5.0f, -20.0f);
		gl.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
		gl.glColor3f(0.0f, 0.0f, 1.0f);
		glu.gluCylinder(quad, 4.0, 6.0, 14.0, 32, 32);
		// bàn tay trái
		gl.glTranslatef(0.0f, 0.0f, -2.0f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		glu.gluSphere(quad, 5.0f, 20, 20);
		gl.glPopMatrix();

		// thân
		gl.glTranslated(0.0f, -3.0f, -21.0f);
		gl.glColor3f(0.0f, 0.0f, 1.0f);

		gl.glPushMatrix();
		gl.glScaled(1.0f, 0.5f, 1.0f);
		gl.glRotated(-90.0f, 1.0f, 0.0f, 0.0f);
		drawPartialSphere(gl, 18.0f, 50, 25, 90.0f);
		gl.glPopMatrix();

		gl.glTranslated(0.0f, -12.0f, 0.0f);
		gl.glPushMatrix();
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		glu.gluDisk(quad, 0.0, 18.0, 32, 1);
		glu.gluCylinder(quad, 18.0, 18.0, 12.0, 32, 32);
		gl.glTranslatef(0.0f, 0.0f, 12.0f);
		glu.gluDisk(quad, 0.0, 18.0, 32, 1);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glScaled(1.0f, 0.5f, 1.0f);
		gl.glRotated(90.0f, 1.0f, 0.0f, 0.0f);
		drawPartialSphere(gl, 18.0f, 50, 25, 90.0f);
		gl.glPopMatrix();

		// bụng
		gl.glPushMatrix();
		gl.glScaled(1.0f, 0.9f, 0.5f);
		gl.glTranslated(0.0f, 7.0f, 25.0f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		drawPartialSphere(gl, 14.0f, 50, 25, 90.0f);
		gl.glPopMatrix();

		// đuôi
		gl.glPushMatrix();
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glTranslatef(0.0f, 0.0f, -21.0f);
		glu.gluSphere(quad, 3.5f, 20, 20);
		gl.glPopMatrix();

		// túi
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 6.0f, 12.0f);
		gl.glScalef(1.2f, 1.0f, 0.9f);
		gl.glRotated(90.0f, 1.0f, 0.0f, 0.0f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		drawPartialSphere(gl, 10.0f, 50, 25, 90.0f);
		gl.glPopMatrix();

		// chân phải
		gl.glPushMatrix();
		if (isWalking) {
		gl.glRotatef((float) Math.sin(speed) * 15, 1.0f, 0.0f, 0.0f);
		}
		gl.glTranslatef(-8.0f, -13.0f, 0.0f);
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		gl.glColor3f(0.0f, 0.0f, 1.0f);
		glu.gluDisk(quad, 0.0, 8.0, 32, 1);
		glu.gluCylinder(quad, 8.0, 10.0, 12.0, 32, 32);
		// bàn chân phải
		gl.glTranslatef(0.0f, 0.0f, -2.0f);
		gl.glScalef(1.0f, 1.0f, 0.5f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		glu.gluSphere(quad, 9.0f, 20, 20);
		gl.glPopMatrix();

		// chân trái
		gl.glPushMatrix();
		if (isWalking) {
		gl.glRotatef((float) Math.sin(speed + Math.PI) * 15, 1.0f, 0.0f, 0.0f);
		}
		gl.glTranslatef(8.0f, -13.0f, 0.0f);
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		gl.glColor3f(0.0f, 0.0f, 1.0f);
		glu.gluDisk(quad, 0.0, 8.0, 32, 1);
		glu.gluCylinder(quad, 8.0, 10.0, 12.0, 32, 32);
		// bàn chân trái
		gl.glTranslatef(0.0f, 0.0f, -2.0f);
		gl.glScalef(1.0f, 1.0f, 0.5f);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		glu.gluSphere(quad, 9.0f, 20, 20);
		gl.glPopMatrix();

	}
	public void drawCake(GL2 gl) {
		gl.glPushMatrix();
		gl.glColor3f(0.824f, 0.412f, 0.118f);
		gl.glScalef(1.0f, 0.4f, 1.0f);
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		drawPartialSphere(gl, 14.0f, 50, 25, 90.0f);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glColor3f(0.824f, 0.549f, 0.251f);
		gl.glScalef(1.0f, 0.2f, 1.0f);
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		glu.gluSphere(quad, 15.0f, 20, 20);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, -1.5f, 0.0f);
		gl.glColor3f(0.435f, 0.306f, 0.216f);
		gl.glScalef(1.0f, 0.3f, 1.0f);
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		glu.gluSphere(quad, 13.0f, 20, 20);
		gl.glPopMatrix();
		
		gl.glTranslatef(0.0f, -3.5f, 0.0f);
		gl.glPushMatrix();
		gl.glColor3f(0.824f, 0.412f, 0.118f);
		gl.glScalef(1.0f, 0.4f, 1.0f);
		gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
		drawPartialSphere(gl, 14.0f, 50, 25, 90.0f);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glColor3f(0.824f, 0.549f, 0.251f);
		gl.glScalef(1.0f, 0.2f, 1.0f);
		gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
		glu.gluSphere(quad, 15.0f, 20, 20);
		gl.glPopMatrix();
		
		cakeSpeed += 15.0f;
	}
	public void randomCakeLocation() {
		Random random = new Random();
		int	x = random.nextInt(map[0].length);
		int	z = random.nextInt(map.length);
		if (map[z][x] == 0) {
			endMapX = x * 10.0f;
			endMapZ = z * 10.0f;
			return;
		} else {
			randomCakeLocation();
		}
	}
	public void checkEnd() {
		if (gameOver) {
			this.setVisible(false);
			javax.swing.SwingUtilities.invokeLater(() -> {
                Lose losePanel = new Lose();
                FPSAnimator loseAnimator = new FPSAnimator(losePanel, 60, true);

                JFrame loseFrame = new JFrame("Bạn đã thắng!");
                loseFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                loseFrame.getContentPane().add(losePanel);
                loseFrame.pack();
                loseFrame.setLocationRelativeTo(null);
                loseFrame.setVisible(true);

                loseAnimator.start();
            });
        	return;
		}
		float distance = (float) Math.sqrt(Math.pow(charMapX - endMapX, 2) + Math.pow(charMapZ - endMapZ, 2));

	    if (distance < 3.0f) {
	        score++;
	        randomCakeLocation();
	        System.out.println("Điểm: " + score);

	        if (score >= 3) {
	        	this.setVisible(false);
	        	javax.swing.SwingUtilities.invokeLater(() -> {
	                Win winPanel = new Win();
	                FPSAnimator winAnimator = new FPSAnimator(winPanel, 60, true);

	                JFrame winFrame = new JFrame("Bạn đã thắng!");
	                winFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	                winFrame.getContentPane().add(winPanel);
	                winFrame.pack();
	                winFrame.setLocationRelativeTo(null);
	                winFrame.setVisible(true);

	                winAnimator.start();
	            });
	        	return;
	        }
	    }
	}

	

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	class KeyHandler extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent key) {
			float newX = charMapX;
			float newZ = charMapZ;

			switch (key.getKeyCode()) {
			case KeyEvent.VK_W:
				newZ += 1;
				movingForward = true;
				isWalking = true;
				rotationY = 0.0f;
				break;
			case KeyEvent.VK_S:
				newZ -= 1;
				movingBackward = true;
				isWalking = true;
				rotationY = 180.0f;
				break;
			case KeyEvent.VK_A:
				newX += 1;
				movingLeft = true;
				isWalking = true;
				rotationY = 90.0f;
				break;
			case KeyEvent.VK_D:
				newX -= 1;
				movingRight = true;
				isWalking = true;
				rotationY = -90.0f;
				break;
			}

			if (newZ / 10 >= 0 && newZ / 10 < map.length && newX / 10 >= 0 && newX / 10 < map[0].length
					&& map[(int) Math.round(newZ / 10)][(int) Math.round(newX / 10)] == 0) {
				charMapX = newX;
				charMapZ = newZ;
				camX = newX;
				camZ = newZ;

			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_W -> movingForward = isWalking = false;
			case KeyEvent.VK_S -> movingBackward = isWalking = false;
			case KeyEvent.VK_A -> movingLeft = isWalking = false;
			case KeyEvent.VK_D -> movingRight = isWalking = false;
			}
		}
	}

	public void refresh() {
		repaint();
	}

}