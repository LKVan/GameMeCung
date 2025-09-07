package main;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL2.*;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class Main extends GLJPanel implements GLEventListener {
	// Define constants for the top-level container
	private static String TITLE = "JOGL 2.0 Setup (GLJPanel)"; // window's title
	private static final int PANEL_WIDTH = 640; // width of the drawable
	private static final int PANEL_HEIGHT = 960; // height of the drawable
	private static final int FPS = 60; // animator's target frames per second

	private GLU glu;
	private GLUT glut;
	private GLUquadric quad;

	float cx = 0.0f, cy = 0.0f, cz = 300.0f;
	private boolean isTalking = true;
	float speed = 0.0f;
	private int mouthToggle = 0;
	private long lastToggleTime = System.currentTimeMillis();

	private float playX = 40.0f;
	private float playY = 30.0f;
	private float playW = 50.0f;
	private float playH = 10.0f;

	private float exitX = 40.0f;
	private float exitY = 0.0f;
	private float exitW = 50.0f;
	private float exitH = 10.0f;

	private Texture menuTexture, playTexture, exitTexture, grassTexture, skyTexture;

	public static void main(String[] args) {
		System.setProperty("java.library.path", "lib");
		// Run the GUI codes in the event-dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create the OpenGL rendering canvas
				GLJPanel canvas = new Main();
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
				frame.setResizable(false);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				animator.start(); // start the animation loop
			}
		});
	}

	public Main() {
		this.addGLEventListener(this);
		this.addMouseListener(new MenuScreen());
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

		try {
			File imgFile = new File("src\\textures\\menu.png");
			File imgFile1 = new File("src\\textures\\play.png");
			File imgFile2 = new File("src\\textures\\exit.png");
			File imgFile3 = new File("src\\textures\\grass.jpg");
			File imgFile4 = new File("src\\textures\\sky.jpg");
			menuTexture = TextureIO.newTexture(imgFile, true);
			playTexture = TextureIO.newTexture(imgFile1, true);
			exitTexture = TextureIO.newTexture(imgFile2, true);
			grassTexture = TextureIO.newTexture(imgFile3, true);
			grassTexture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
			grassTexture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
			skyTexture = TextureIO.newTexture(imgFile4, true);

		} catch (IOException e) {
			e.printStackTrace();
		}

		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
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
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		// Camera
		gl.glTranslatef(-cx, -cy, -cz);

		gl.glPushMatrix();
		menuTexture.enable(gl);
		menuTexture.bind(gl);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glTranslatef(0.0f, 90.0f, 0.0f);
		gl.glScalef(110.0f, 40.0f, 1.0f);
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0f, 0f);
		gl.glVertex2f(-1f, -1f);
		gl.glTexCoord2f(1f, 0f);
		gl.glVertex2f(1f, -1f);
		gl.glTexCoord2f(1f, 1f);
		gl.glVertex2f(1f, 1f);
		gl.glTexCoord2f(0f, 1f);
		gl.glVertex2f(-1f, 1f);
		gl.glEnd();
		menuTexture.disable(gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		playTexture.enable(gl);
		playTexture.bind(gl);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glTranslatef(40.0f, 10.0f, 0.0f);
		gl.glScalef(50.0f, 20.0f, 1.0f);
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0f, 0f);
		gl.glVertex2f(-1f, -1f);
		gl.glTexCoord2f(1f, 0f);
		gl.glVertex2f(1f, -1f);
		gl.glTexCoord2f(1f, 1f);
		gl.glVertex2f(1f, 1f);
		gl.glTexCoord2f(0f, 1f);
		gl.glVertex2f(-1f, 1f);
		gl.glEnd();
		playTexture.disable(gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		exitTexture.enable(gl);
		exitTexture.bind(gl);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glTranslatef(40.0f, -40.0f, 0.0f);
		gl.glScalef(50.0f, 20.0f, 1.0f);
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0f, 0f);
		gl.glVertex2f(-1f, -1f);
		gl.glTexCoord2f(1f, 0f);
		gl.glVertex2f(1f, -1f);
		gl.glTexCoord2f(1f, 1f);
		gl.glVertex2f(1f, 1f);
		gl.glTexCoord2f(0f, 1f);
		gl.glVertex2f(-1f, 1f);
		gl.glEnd();
		exitTexture.disable(gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		float[] lightPos = { -50.0f, 50.0f, 50.0f, 1.0f };
		float[] lightColor = { 1f, 1f, 1f, 1f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightColor, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightColor, 0);

		gl.glTranslatef(-60.0f, 5.0f, 0.0f);
		drawHead(gl);
		gl.glTranslated(0.0f, -29.0f, 0.0f);
		drawBody(gl);
		speed += 0.15f;
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glEnable(GL2.GL_TEXTURE_2D);
		grassTexture.bind(gl);
		gl.glTranslatef(0.0f, -80.0f, 0.0f);
		gl.glRotatef(10f, 1f, 0f, 0f);
		gl.glScalef(200.0f, 1.0f, 200.0f);

		gl.glBegin(GL2.GL_QUADS);
		gl.glNormal3f(0f, 1f, 0f);
		gl.glTexCoord2f(0f, 0f);
		gl.glVertex3f(-1f, 0f, -1f);
		gl.glTexCoord2f(4f, 0f);
		gl.glVertex3f(1f, 0f, -1f);
		gl.glTexCoord2f(4f, 4f);
		gl.glVertex3f(1f, 0f, 1f);
		gl.glTexCoord2f(0f, 4f);
		gl.glVertex3f(-1f, 0f, 1f);
		gl.glEnd();
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glEnable(GL2.GL_TEXTURE_2D);
		skyTexture.bind(gl);
		gl.glTranslatef(0.0f, 10.0f, -100.0f); 
		gl.glScalef(300.0f, 120.0f, 1.0f);
		gl.glBegin(GL2.GL_QUADS);
		gl.glNormal3f(0f, 0f, 1f);
		gl.glTexCoord2f(0f, 0f);
		gl.glVertex3f(-0.5f, -0.5f, 0f);
		gl.glTexCoord2f(1f, 0f);
		gl.glVertex3f(0.5f, -0.5f, 0f);
		gl.glTexCoord2f(1f, 1f);
		gl.glVertex3f(0.5f, 0.5f, 0f);
		gl.glTexCoord2f(0f, 1f);
		gl.glVertex3f(-0.5f, 0.5f, 0f);
		gl.glEnd();
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glPopMatrix();

	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	public void refresh() {
		repaint();
	}

	// vẽ nửa hình cầu
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

		// mắt trái
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

		// mắt phải
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

		// miệng
		if (isTalking) {
			// 300ms
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastToggleTime > 400) {
				mouthToggle = 1 - mouthToggle; // 0 <-> 1
				lastToggleTime = currentTime;
			}

			if (mouthToggle == 0) {
				// Miệng 1
				gl.glPushMatrix();
				gl.glColor3f(1.0f, 0.0f, 0.0f);
				gl.glTranslatef(0.0f, -16.0f, 25.5f);
				gl.glRotated(37.0f, 1.0f, 0.0f, 0.0f);
				gl.glScalef(1.8f, 1.1f, 0.40f);
				drawPartialSphere(gl, 8.0f, 50, 25, 90.0f);
				gl.glPopMatrix();

				gl.glPushMatrix();
				gl.glColor3f(1.0f, 0.5f, 0.0f);
				gl.glTranslatef(0.0f, -17.0f, 25.7f);
				gl.glRotated(37.0f, 1.0f, 0.0f, 0.0f);
				gl.glScalef(1.5f, 0.8f, 0.40f);
				drawPartialSphere(gl, 8.0f, 50, 25, 90.0f);
				gl.glPopMatrix();
			} else {
				// Miệng 2
				gl.glPushMatrix();
				gl.glColor3f(1.0f, 0.0f, 0.0f);
				gl.glTranslatef(0.0f, -16.0f, 25.5f);
				gl.glRotated(37.0f, 1.0f, 0.0f, 0.0f);
				gl.glScalef(0.6f, 0.6f, 0.40f);
				drawPartialSphere(gl, 8.0f, 50, 25, 90.0f);
				gl.glPopMatrix();
			}
		} else {
			gl.glPushMatrix();
			gl.glColor3f(1.0f, 0.0f, 0.0f);
			gl.glTranslatef(0.0f, -16.0f, 25.5f);
			gl.glRotated(37.0f, 1.0f, 0.0f, 0.0f);
			gl.glScalef(0.6f, 0.6f, 0.40f);
			drawPartialSphere(gl, 8.0f, 50, 25, 90.0f);
			gl.glPopMatrix();
		}

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
		gl.glRotatef(-(float) Math.sin(speed) * 10, 0.0f, 0.0f, 1.0f);
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
		gl.glRotatef((float) Math.sin(speed) * 10, 0.0f, 0.0f, 1.0f);
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
	}

	public void exit() {
		this.setVisible(false);
	}

	class MenuScreen implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			int mouseX = e.getX();
			int mouseY = e.getY();

			// Chuyển đổi sang tọa độ OpenGL
			float glX = (mouseX - PANEL_WIDTH / 2f) / (PANEL_WIDTH / 2f) * 100f;
			float glY = -(mouseY - PANEL_HEIGHT / 2f) / (PANEL_HEIGHT / 2f) * 100f;

			if (glX > playX - playW && glX < playX + playW && glY > playY - playH && glY < playY + playH) {
				System.out.println("PLAY CLICKED!");
				javax.swing.SwingUtilities.invokeLater(() -> {
					Game playPanel = new Game();
					FPSAnimator playAnimator = new FPSAnimator(playPanel, 60, true);

					JFrame playFrame = new JFrame("PLAY");
					playFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					playFrame.getContentPane().add(playPanel);
					playFrame.pack();
					playFrame.setLocationRelativeTo(null);
					playFrame.setVisible(true);
					playAnimator.start();
				});
				exit();

			}

			if (glX > exitX - exitW && glX < exitX + exitW && glY > exitY - exitH && glY < exitY + exitH) {
				System.out.println("EXIT CLICKED!");
				System.exit(0);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	}

}