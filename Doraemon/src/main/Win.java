package main;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL2.*;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class Win extends GLJPanel implements GLEventListener {

    private GLU glu;
    private GLUquadric quad;
    private Texture winTexture;

    public Win() {
        this.setPreferredSize(new Dimension(640, 960));
        this.addGLEventListener(this);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        quad = glu.gluNewQuadric();
        glu.gluQuadricDrawStyle(quad, GLU.GLU_FILL);
        glu.gluQuadricNormals(quad, GLU.GLU_SMOOTH);

        gl.glClearColor(1f, 1f, 1f, 0f);
        gl.glClearDepth(1f);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        gl.glShadeModel(GL_SMOOTH);

        try {
            winTexture = TextureIO.newTexture(new File("src/textures/win.png"), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        if (height == 0) height = 1;
        float aspect = (float) width / height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, aspect, 10.0, 500.0);

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glTranslated(0.0f, 0.0f, -30.0f);
        drawEnd(gl);
    }

    private void drawEnd(GL2 gl) {
        if (winTexture == null) return;

        gl.glEnable(GL2.GL_TEXTURE_2D);
        winTexture.bind(gl);

        float size = 8.5f;
        gl.glBegin(GL2.GL_QUADS);

        // Front face
        gl.glTexCoord2f(0f, 0f); gl.glVertex3f(-size, -size,  size);
        gl.glTexCoord2f(1f, 0f); gl.glVertex3f( size, -size,  size);
        gl.glTexCoord2f(1f, 1f); gl.glVertex3f( size,  size,  size);
        gl.glTexCoord2f(0f, 1f); gl.glVertex3f(-size,  size,  size);

        // Back face
        gl.glTexCoord2f(0f, 0f); gl.glVertex3f( size, -size, -size);
        gl.glTexCoord2f(1f, 0f); gl.glVertex3f(-size, -size, -size);
        gl.glTexCoord2f(1f, 1f); gl.glVertex3f(-size,  size, -size);
        gl.glTexCoord2f(0f, 1f); gl.glVertex3f( size,  size, -size);

        // Left face
        gl.glTexCoord2f(0f, 0f); gl.glVertex3f(-size, -size, -size);
        gl.glTexCoord2f(1f, 0f); gl.glVertex3f(-size, -size,  size);
        gl.glTexCoord2f(1f, 1f); gl.glVertex3f(-size,  size,  size);
        gl.glTexCoord2f(0f, 1f); gl.glVertex3f(-size,  size, -size);

        // Right face
        gl.glTexCoord2f(0f, 0f); gl.glVertex3f( size, -size,  size);
        gl.glTexCoord2f(1f, 0f); gl.glVertex3f( size, -size, -size);
        gl.glTexCoord2f(1f, 1f); gl.glVertex3f( size,  size, -size);
        gl.glTexCoord2f(0f, 1f); gl.glVertex3f( size,  size,  size);

        // Top face
        gl.glTexCoord2f(0f, 0f); gl.glVertex3f(-size,  size,  size);
        gl.glTexCoord2f(1f, 0f); gl.glVertex3f( size,  size,  size);
        gl.glTexCoord2f(1f, 1f); gl.glVertex3f( size,  size, -size);
        gl.glTexCoord2f(0f, 1f); gl.glVertex3f(-size,  size, -size);

        // Bottom face
        gl.glTexCoord2f(0f, 0f); gl.glVertex3f(-size, -size, -size);
        gl.glTexCoord2f(1f, 0f); gl.glVertex3f( size, -size, -size);
        gl.glTexCoord2f(1f, 1f); gl.glVertex3f( size, -size,  size);
        gl.glTexCoord2f(0f, 1f); gl.glVertex3f(-size, -size,  size);

        gl.glEnd();
        gl.glDisable(GL2.GL_TEXTURE_2D);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    public void refresh() {
        repaint();
    }
    public static void main(String[] args) {
    	System.setProperty("java.library.path", "lib");
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
	}
}
