import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Supplier;

public final class Geom3D {

    // is parsed from an obj tracking verticies and their order from the obj file
    public static class Mesh {
        private final ArrayList<P3D> vertices;
        private final ArrayList<Calc.Triple<Integer>> vertexOrder;

        public Mesh(ArrayList<P3D> vertices, ArrayList<Calc.Triple<Integer>> vertexOrder) {
            this.vertices = vertices;
            this.vertexOrder = vertexOrder;
        }

        public void draw(Utils.Camera camera, Graphics g) {
            Plane3D screen = camera.getViewPlane(50, 1, 1);
            ArrayList<FaceInfo> faces = new ArrayList<>(), res = new ArrayList<>();

            for (Calc.Triple<Integer> face : vertexOrder) {
                Plane3D facet = new Plane3D(vertices.get(face.getA() - 1),vertices.get(face.getB() - 1),vertices.get(face.getC() - 1));

                Calc.Vector normal = facet.getUnitVectorNormal();
                Calc.Vector screenNorm = screen.getUnitVectorNormal();

                double dot = screenNorm.getDotProduct(normal);
                Color color = new Color((int) (255*dot));

                if (dot > 0) {
                    faces.add(new FaceInfo(facet, color, facet.getCenter().getDistance(camera.getOrigin.get())));
                }
            }

            //Collections.sort(faces);
            Collections.reverse(faces);

            faces.forEach(face -> face.draw(screen, g));
        }

        // allow sorting by distance from camera (didnt work)
        public static class FaceInfo implements Comparable<FaceInfo> {

            private final double dist;
            private final Plane3D face;
            private final Color color;

            public FaceInfo(Plane3D face, Color color, double dist) {
                this.dist = dist;
                this.face = face;
                this.color = color;
            }

            public void draw(Plane3D screen, Graphics g) {
                g.setColor(color);
                Gui.fillFace(screen.getOrtho2DProject(face.a.get()), screen.getOrtho2DProject(face.b.get()),screen.getOrtho2DProject(face.c.get()), g);
            }

            private double getDist() {
                return dist;
            }

            @Override
            public String toString() {
                return dist + "";
            }

            @Override
            public int compareTo(FaceInfo o) {
                return (int) -(getDist()-o.getDist());
            }
        }
    }

    // plane, defined by a triangle
    public static class Plane3D {

        //get axis through the plane
        public final Supplier<Line3D> xAxis, yAxis, zAxis;
        public final Supplier<P3D> a, b, c, d;
        private final Calc.Triple<P3D> t;

        public Plane3D(P3D a, P3D b, P3D c) {
            t = new Calc.Triple<>(a, b, c);
            this.a = t::getA; this.b = t::getB; this.c = t::getC;
            d = () -> new Line3D(this.a.get(), new Line3D(this.b.get(), this.c.get()).getMidpoint()).getPoint(2); // the fourth point of the triangle to make a rhombus
            xAxis = () -> new Line3D(new Line3D(this.a.get(), this.b.get()).getMidpoint(), new Line3D(d.get(), this.c.get()).getMidpoint());
            yAxis = () -> new Line3D(new Line3D(this.a.get(), this.c.get()).getMidpoint(), new Line3D(d.get(), this.b.get()).getMidpoint());
            zAxis = () -> new Line3D(new Line3D(this.b.get(), this.c.get()).getMidpoint(), getUnitVectorNormal().getEndPoint(new Line3D(this.b.get(), this.c.get()).getMidpoint()));

        }

        // 2d projection of an intersection relative to triangle.a
        public Point2D getOrtho2DProject(P3D vertex) {
            P3D intersect = getOrtho3DProject(vertex);
            // coplanar reflect across point a
            P3D reflect = new Line3D(intersect, a.get()).getPoint(2);

            // angle correction, when the intersection point is further from c than the reflection that means the point is on top, and the range is 0 -> 180, else its on bottom range 180 -> 360
            double angle = (reflect.getDistance(c.get()) < intersect.getDistance(c.get()))?Calc.getAngle(a.get(), b.get(), intersect):(Math.PI*2)-Calc.getAngle(a.get(), b.get(), intersect);

            double radius = intersect.getDistance(a.get());
            double x = radius * Math.cos(angle), y = radius * Math.sin(angle);

            // nan case where the projection point is on point a
            return (Double.isNaN(x) || Double.isNaN(y))?new Point2D.Double(0,0):new Point2D.Double(x, y);
        }

        // 3d projection of a point onto the plane by intersecting a line made by the point and the planes vector normal
        public P3D getOrtho3DProject(P3D vertex) {return getIntersect(new Line3D(vertex, getVectorNormal().getEndPoint(vertex)));}

        // get intersection with a line using the parametric equation of a plane and solving for t with the line (works in all circumstances)
        public P3D getIntersect(Line3D line) {
            Calc.Vector vN = getVectorNormal();

            double a = vN.getA();
            double b = vN.getB();
            double c = vN.getC();
            double dx = line.XF() - line.XI();
            double dy = line.YF() - line.YI();
            double dz = line.ZF() - line.ZI();

            double d = a * t.getA().getA() + b * t.getA().getB() + c * t.getA().getC();
            double leftT1 = a * line.XI() + b * line.YI() + c * line.ZI();
            double tScalar = a * dx + b * dy + c * dz;

            double t = (d - leftT1) / tScalar;

            return new P3D(
                    line.XI() + (dx * t),
                    line.YI() + (dy * t),
                    line.ZI() + (dz * t)
            );
        }

        public P3D getCenter() {return new Line3D(b.get(), c.get()).getMidpoint();}

        // get the unit vector of the normal vector
        public Calc.Vector getUnitVectorNormal() {return getVectorNormal().getUnitVector();}

        // get the normal vector of the plane using a cross product of 2 coplanar vectors
        public Calc.Vector getVectorNormal() {return new Calc.Vector(t.getA(), t.getB()).getCrossProduct(new Calc.Vector(t.getA(), t.getC()));}

        // rotate all the points about an axis
        public void rotate(Line3D axis, double deg) {t.affectAll(point -> point.rotateAroundAxis(axis,deg));}

        // move
        public void translate(double dx, double dy, double dz) {t.affectAll(point -> point.translate(dx, dy, dz));}

        // debug
        public void draw(Plane3D screen, Graphics g) {
            g.setColor(Color.red);
            Gui.drawPoint(screen.getOrtho2DProject(a.get()), g, 4);
            g.setColor(Color.green);
            Gui.drawPoint(screen.getOrtho2DProject(b.get()), g, 4);
            g.setColor(Color.blue);
            Gui.drawPoint(screen.getOrtho2DProject(c.get()), g, 4);
            g.setColor(Color.yellow);
            Gui.drawPoint(screen.getOrtho2DProject(d.get()), g, 4);

            g.setColor(Color.white);
            Gui.drawLine(screen.getOrtho2DProject(a.get()), screen.getOrtho2DProject(c.get()), g);
            Gui.drawLine(screen.getOrtho2DProject(b.get()), screen.getOrtho2DProject(d.get()), g);
            Gui.drawLine(screen.getOrtho2DProject(a.get()), screen.getOrtho2DProject(b.get()), g);
            Gui.drawLine(screen.getOrtho2DProject(c.get()), screen.getOrtho2DProject(d.get()), g);

            Gui.drawPoint(screen.getOrtho2DProject(new Line3D(b.get(), c.get()).getMidpoint()), g, 4);
            Gui.drawPoint(screen.getOrtho2DProject(getUnitVectorNormal().getScaled(50).getEndPoint(new Line3D(b.get(), c.get()).getMidpoint())), g, 4);
        }

        @Override
        public String toString() {return t.toString();}
    }

    // line, defined by two points
    public static class Line3D extends Calc.Tuple<P3D> {

        public Line3D(P3D a, P3D b) {super(a, b);}

        public double XI() {return getA().getA();}
        public double YI() {return getA().getB();}
        public double ZI() {return getA().getC();}
        public double XF() {return getB().getA();}
        public double YF() {return getB().getB();}
        public double ZF() {return getB().getC();}

        // gets the parametric percentage of some given length
        public double getT(double length) {return length/getLength();}

        // 3d length
        public double getLength() {return getVector().getLength();}

        // draws a line perpendicular to this from point p and returns the point it intersects this line at
        // uses "percentage" parametric ex: .5 -> halfway down the line
        public P3D getPerpendicularPointOnLine(P3D point) {
            double angle = Calc.getAngle(getA(), point, getB());
            //use trig to find the opposite side from the angle
            double length = new Line3D(getA(), point).getLength() * Math.cos(angle);

            return getPoint(length / getLength());
        }

        // 3d midpoint
        public P3D getMidpoint() {return new P3D(((XI() + XF()) / 2), ((YI() + YF()) / 2), ((ZI() + ZF()) / 2));}

        // point p for parametric value t
        public P3D getPoint(double t) {
            return new P3D(
                    XI() + (t * (XF() - XI())),
                    YI() + (t * (YF() - YI())),
                    ZI() + (t * (ZF() - ZI()))
            );
        }

        // vector of the line <dx,dy,dz>
        public Calc.Vector getVector() {return new Calc.Vector(getA(), getB());}

        public Calc.Vector getUnitVector() {return getVector().getUnitVector();}
    }

    // point, defined by x,y,z values (abc->xyz)
    public static class P3D extends Calc.Triple<Double> {
        public P3D(double x, double y, double z) {super(x,y,z);}

        public double getDistance(P3D other) {return new Calc.Vector(this, other).getLength();}

        public void translate(double dx, double dy, double dz) {super.setABC(getA()+dx, getB()+dy, getC()+dz);}

        // generates a circle perpendicular to an axis and moves a point along it. takes the point and generates a line to that point,
        // then using that line creates a cross product vector perpendicular to the axis and line from point
        // creates a plane using the end of the cross product line the input point and the intersection of the point to the axis
        // creates the parametric circle for where the input point is, on the plane created
        public void rotateAroundAxis(Geom3D.Line3D axis, double deg) {
            Geom3D.P3D center = axis.getPerpendicularPointOnLine(this); // (works)

            // cross product generated perpendicular vector between c>p1 and c>axis.b (works)
            Calc.Vector cpV2 = new Calc.Vector(center,this).getUnitVector().getCrossProduct(axis.getUnitVector()).getUnitVector();

            // center point unit vector (works)
            Calc.Vector cpV1 = new Calc.Vector(center, this).getUnitVector();

            // radius to the point from the center (works)
            double r = new Geom3D.Line3D(center, this).getLength();

            // nan check if point is on axis
            if (cpV1.notNan() && cpV2.notNan()) {
                // parametric equation for a planar circle and the inputs required for the new degree
                setABC(
                        center.getA() + r * Math.cos(Calc.degToRad.apply(deg)) * cpV1.getA() + r * Math.sin(Calc.degToRad.apply(deg)) * cpV2.getA(),
                        center.getB() + r * Math.cos(Calc.degToRad.apply(deg)) * cpV1.getB() + r * Math.sin(Calc.degToRad.apply(deg)) * cpV2.getB(),
                        center.getC() + r * Math.cos(Calc.degToRad.apply(deg)) * cpV1.getC() + r * Math.sin(Calc.degToRad.apply(deg)) * cpV2.getC()
                );
            }
        }

        @Override
        public String toString() {return "(" + super.toString() + ")";}
    }
}