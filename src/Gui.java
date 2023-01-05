import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// graphics
public final class Gui {

    // draw a point using something's graphics
    public static void drawPoint(Point2D p, Graphics g, int size) {g.drawRoundRect((int)(p.getX()-(size/2)), (int) (p.getY()-(size/2)), size,size,size,size );}

    // draw a line using something's graphics
    public static void drawLine(Point2D a, Point2D b, Graphics g) {g.drawLine((int)a.getX(),(int) a.getY(),(int) b.getX(), (int)b.getY());}

    // fill tri
    public static void fillFace(Point2D a, Point2D b, Point2D c, Graphics g) {g.fillPolygon(new int[]{(int) a.getX(), (int) b.getX(), (int) c.getX()}, new int[] {(int) a.getY(), (int) b.getY(), (int) c.getY()}, 3);}

    // implementation of JPanel with a graphics "collection/overlay" system
    public static class Panel extends JPanel {
        private final ArrayList<Calc.Pair<BufferedImage, Calc.Pair<Point2D, Double>>> buffers = new ArrayList<>();

        public Panel(Dimension size) {
            setSize(size);
            setPreferredSize(size);
            setVisible(true);
        }

        // get one of the layers graphics
        public Graphics getBufferGraphics(int index) {return buffers.get(index).getA().getGraphics();}

        // add a new layer at some location on the panel, from the top left corner of the layer, drawn to some scale onto the panel
        public void addBuffer(BufferedImage buffer, Point2D loc, double scale) {buffers.add(new Calc.Pair<>(buffer, new Calc.Pair<>(loc, scale)));}

        // draw all the layers at their locations and scales
        public void paint(Graphics g) {
            g.clearRect(0, 0, getWidth(), getHeight());

            for (Calc.Pair<BufferedImage, Calc.Pair<Point2D, Double>> buffer : buffers) {

                g.drawImage(buffer.getA(),
                        0, 0,
                        (int)(buffer.getB().getB()*buffer.getA().getWidth()),
                        (int)(buffer.getB().getB()*buffer.getA().getHeight()),
                        0,0,
                        buffer.getA().getWidth(),
                        buffer.getA().getHeight(),

                        null
                );

                buffer.getA().getGraphics().clearRect(0, 0, buffer.getA().getWidth(), buffer.getA().getHeight());
            }
        }
    }

    // jFrame
    public static class Frame extends JFrame {
        public Frame() {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setResizable(false);
            setVisible(true);
        }
    }

    // implementation of KeyListener where inputs are obtainable
    public static class Listener implements KeyListener {

        private final ArrayList<Integer> inputs = new ArrayList<>();
        private Integer input = null;

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (!inputs.contains(e.getKeyCode())) {
                inputs.add(e.getKeyCode());
            }

            input = e.getKeyCode();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            inputs.removeIf(input -> input.equals(e.getKeyCode()));

            if (inputs.size() == 0) {
                input = null;
            } else if (inputs.size() == 1) {
                input = inputs.get(0);
            }
        }

        public ArrayList<Integer> getInputs() {
            return inputs;
        }

        public Integer getCurrentInput() {
            return input;
        }
    }

    //  drawable interface to compile all drawable things into one spot not used :/
    public interface Drawable {
        void drawSelf(Graphics g);
    }
}
