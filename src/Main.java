import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;

// x = left/right y = fwd/back z = up/down
public class Main {
    public static Dimension size = new Dimension(1000, 750);

    public static Geom3D.Plane3D plane = new Geom3D.Plane3D(new Geom3D.P3D(0,150,0),new Geom3D.P3D(100, 150, 0),new Geom3D.P3D(0, 150, 100));
    public static Geom3D.Mesh mesh = Utils.parseObj("./OBJ/bulbasaur.obj", 1);

    public static Utils.Camera camera = new Utils.Camera();

    public static Gui.Frame frame = new Gui.Frame();
    public static Gui.Panel panel = new Gui.Panel(size);
    public static Gui.Listener listener = new Gui.Listener();

    public static Utils.ExtendableThread main = new Utils.ExtendableThread() {
        @Override
        public void execute() throws InterruptedException {
            try {listener.getInputs().forEach(input -> camera.move(input));}
            catch (ConcurrentModificationException e) {System.out.println("error");}

            mesh.draw(camera, panel.getBufferGraphics(0));

            panel.repaint();

            wait(1000/60);
        }

        @Override
        public boolean condition() {return true;}
    };

    public static void main(String[] args) {
        frame.addKeyListener(listener);
        frame.setTitle("3d");

        panel.addBuffer(new BufferedImage(size.width, size.height, Image.SCALE_DEFAULT), new Point2D.Double(0,0), 1);
        
        camera.translate(-400, 0, 600);

        frame.add(panel);
        frame.pack();

        main.start();
    }
}
