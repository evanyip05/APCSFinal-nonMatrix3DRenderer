import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Supplier;

public final class Utils {

    public static Geom3D.Mesh parseObj(String dir, double scale) {
        ArrayList<String> verticies = new ArrayList<>();
        ArrayList<String> faces = new ArrayList<>();
        ArrayList<String> file = fileToStringArr(dir);
        ArrayList<Geom3D.P3D> verticiesParsed = new ArrayList<>();
        ArrayList<Calc.Triple<Integer>> vertexOrder = new ArrayList<>();

        for (String line : file) {
            if (line.startsWith("v")&&!line.startsWith("n", 1)&&!line.startsWith("t", 1)) {
                verticies.add(line);
            }

            if (line.startsWith("f")) {
                faces.add(line);
            }
        }

        for (String vertex : verticies) {
            String[] vertexSplit = vertex.split("[\\s]");
            verticiesParsed.add(new Geom3D.P3D(
                    Double.parseDouble(vertexSplit[1])*scale,
                    Double.parseDouble(vertexSplit[2])*scale,
                    Double.parseDouble(vertexSplit[3])*scale)
            );
        }

        for (String face : faces) {
            String[] faceSplit = face.split("[\\s]");

            if (face.contains("/")) {
                vertexOrder.add(new Calc.Triple<>(
                        Integer.parseInt(faceSplit[1].split("[/]+")[0]),
                        Integer.parseInt(faceSplit[2].split("[/]+")[0]),
                        Integer.parseInt(faceSplit[3].split("[/]+")[0])
                        )
                );
            } else {
                vertexOrder.add(new Calc.Triple<>(
                        Integer.parseInt(faceSplit[1]),
                        Integer.parseInt(faceSplit[2]),
                        Integer.parseInt(faceSplit[2])
                        )
                );
            }
        }

        return new Geom3D.Mesh(verticiesParsed, vertexOrder);
    }

    // parse a file to a string
    private static ArrayList<String> fileToStringArr(String dir) {
        ArrayList<String> file = new ArrayList<>();

        try {
            Scanner reader = new Scanner(Path.of(dir));

            while (reader.hasNext()) {
                file.add(reader.nextLine());
            }

            return file;
        } catch (IOException e) {
            return file;
        }
    }

    // a thread implementation used in my other programs. restartable using wait/notify syncing,
    // usually used through anon declarations
    public abstract static class ExtendableThread extends Thread {

        // necessary method from the thread class from the runnable interface providing a separate thread
        // task -> waitCond?wait
        @Override
        public final void run() {
            while (condition()) {
                synchronized (this) {
                    try {
                        execute();

                        if (waitCondition()) {
                            wait();
                        }

                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }

        // process that happens if a thread is to be restarted
        public final void restart() {
            synchronized (this) {
                if (getState().equals(State.NEW)) {
                    start();
                }

                executeOnRestart();
                notify();
            }
        }

        // default code block to be run when the thread resumes
        public void executeOnRestart() {}

        // default condition for which the thread should pause, can be overridden
        public boolean waitCondition() {return false;}

        // the task the thread performs
        public abstract void execute() throws InterruptedException;

        // the condition on which the thread should keep performing the task
        public abstract boolean condition();
    }

    public static class Camera extends OriginCube{
        // x is foward
        // y is right
        // z is up

        public Geom3D.Plane3D getViewPlane(double dist, double width, double height) {
            Calc.Vector lr = new Calc.Vector(getX.get(), getOrigin.get()).getUnitVector().getScaled(width).getInverted();
            Calc.Vector ud = new Calc.Vector(getZ.get(), getOrigin.get()).getUnitVector().getScaled(height).getInverted();

            Geom3D.Line3D forward = new Geom3D.Line3D(getOrigin.get(), getX.get());
            Geom3D.P3D center = forward.getPoint(forward.getT(dist));

            return new Geom3D.Plane3D(center, lr.getEndPoint(center), ud.getEndPoint(center));
        }

    }

    public static class OriginCube {
        private final Geom3D.P3D origin, x, y, z;
        public final Supplier<Geom3D.P3D> getOrigin, getX, getY, getZ;

        public OriginCube() {
            origin = new Geom3D.P3D( 0, 0,  0);
            x      = new Geom3D.P3D(50, 0,  0);
            y      = new Geom3D.P3D( 0,50,  0);
            z      = new Geom3D.P3D( 0, 0, 50);

            getOrigin = () -> origin;
            getX = () -> x;
            getY = () -> y;
            getZ = () -> z;
        }

        public void draw(Geom3D.Plane3D screen, Graphics g) {
            g.setColor(Color.white);
            Gui.drawPoint(screen.getOrtho2DProject(origin), g, 4);
            g.setColor(Color.RED);
            Gui.drawPoint(screen.getOrtho2DProject(x), g, 4);
            Gui.drawLine(screen.getOrtho2DProject(origin), screen.getOrtho2DProject(x), g);
            g.setColor(Color.GREEN);
            Gui.drawPoint(screen.getOrtho2DProject(y), g, 4);
            Gui.drawLine(screen.getOrtho2DProject(origin), screen.getOrtho2DProject(y), g);
            g.setColor(Color.BLUE);
            Gui.drawPoint(screen.getOrtho2DProject(z), g, 4);
            Gui.drawLine(screen.getOrtho2DProject(origin), screen.getOrtho2DProject(z), g);
        }

        public void move(Integer input) {
            double deg = 3, delta = 5;

            switch (input) {
                case 87: translate( 0, -delta,  0); break; //forward (w)
                case 83: translate( 0,  delta,  0); break; //backwards (s)
                case 65: translate(-delta,  0,   0); break; //left (a)
                case 68: translate( delta,  0,   0); break; //right (d)
                case 16: translate( 0,  0,  -delta); break; //down (shift)
                case 32: translate( 0,  0,   delta); break; //up (space)
                case 38: rotateX( deg);break; //pitch up (upArrow)
                case 40: rotateX(-deg);break; //pitch down (downArrow)
                case 34: rotateY(-deg);break; //roll left (pgDown)
                case 33: rotateY( deg);break; //roll right (pgUp)
                case 39: rotateZ( deg);break; //turn right (rightArrow)
                case 37: rotateZ(-deg);break; //turn left (leftArrow)
                default: System.out.println(input);
            }
        }

        public void rotateX(double deg) {
            y.rotateAroundAxis(new Geom3D.Line3D(origin, x), deg);
            z.rotateAroundAxis(new Geom3D.Line3D(origin, x), deg);
        }

        public void rotateY(double deg) {
            x.rotateAroundAxis(new Geom3D.Line3D(origin, y), deg);
            z.rotateAroundAxis(new Geom3D.Line3D(origin, y), deg);
        }

        public void rotateZ(double deg) {
            x.rotateAroundAxis(new Geom3D.Line3D(origin, z), deg);
            y.rotateAroundAxis(new Geom3D.Line3D(origin, z), deg);
        }

        public void translate(double dx, double dy, double dz) {
            origin.translate(dx, dy, dz);
            x.translate(dx, dy, dz);
            y.translate(dx, dy, dz);
            z.translate(dx, dy, dz);
        }
    }

}
