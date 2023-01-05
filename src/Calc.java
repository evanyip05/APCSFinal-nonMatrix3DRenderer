import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Calc {

    // basic math
    public static Function<Double, Double> degToRad = deg -> (deg*Math.PI)/180;
    public static Function<Double, Double> radToDeg = rad -> (rad*180)/Math.PI;

    // round to 3 places (mostly for readability during debug)
    public static Function<Double, Double> roundDouble = (d) ->
            (((d + "").split("[.]")[1].charAt(0) == '9')) ?
                    Double.parseDouble(new DecimalFormat("#").format(d))    :
                    Double.parseDouble(new DecimalFormat("#.###").format(d));

    // assume that points are coplanar. can't use standard trig; though points are coplanar, the only thing that guarantees is length
    // law of cosines works here, returns radians only from 0 to 180, so there's issues where 270 -> 90
    public static double getAngle(Geom3D.P3D apex, Geom3D.P3D a, Geom3D.P3D b) {
        double apexA   = new Geom3D.Line3D(apex, a).getLength();
        double apexB   = new Geom3D.Line3D(apex, b).getLength();
        double oppApex = new Geom3D.Line3D(a   , b).getLength();

        return Math.acos((((apexA*apexA)+(apexB*apexB))-(oppApex*oppApex))/(2*apexA*apexB));
    }

    // move a plane using some integer input
    public static void movePlane(Geom3D.Plane3D p, Integer input, double transMag, double rotMag) {
        switch (input) {
            case 87: p.translate( 0, -transMag,  0); break; //forward (w)
            case 83: p.translate( 0,  transMag,  0); break; //backwards (s)
            case 65: p.translate(-transMag,  0,   0); break; //left (a)
            case 68: p.translate( transMag,  0,   0); break; //right (d)
            case 16: p.translate( 0,  0,  -transMag); break; //down (shift)
            case 32: p.translate( 0,  0,   transMag); break; //up (space)
            case 38: p.rotate(p.yAxis.get(),  rotMag); break; //pitch up (upArrow)
            case 40: p.rotate(p.yAxis.get(), -rotMag); break; //pitch down (downArrow)
            case 34: p.rotate(p.zAxis.get(), -rotMag); break; //roll left (pgDown)
            case 33: p.rotate(p.zAxis.get(),  rotMag); break; //roll right (pgUp)
            case 39: p.rotate(p.xAxis.get(), -rotMag); break; //turn right (rightArrow)
            case 37: p.rotate(p.xAxis.get(),  rotMag); break; //turn left (leftArrow)
            default: System.out.println(input);
        }
    }

    // vector and vector math
    public static class Vector extends Triple<Double> {

        // manual deltas
        public Vector(Double a, Double b, Double c) {super(a, b, c);}

        // point deltas
        public Vector(Geom3D.P3D a, Geom3D.P3D b) {super(b.getA()-a.getA(), b.getB()-a.getB(), b.getC()-a.getC());}

        public boolean notNan() {return !Double.isNaN(getA()) && !Double.isNaN(getB()) && !Double.isNaN(getC());}

        // dot product between 2 vectors (does a comparison of 2 vectors, returning percentage of their similarity)
        public double getDotProduct(Vector other) {return getA()*other.getA()+getB()*other.getB()+getC()*other.getC();}

        public double getLength() {return Math.sqrt(getA()*getA()+getB()*getB()+getC()*getC());}

        // cross product between 2 vectors, perpendicular vector to the two (note to self: vectors are deltas apex doesn't matter)
        public Vector getCrossProduct(Vector other) {
            return new Vector(
                    getB() * other.getC() - getC() * other.getB(),
                    getC() * other.getA() - getA() * other.getC(),
                    getA() * other.getB() - getB() * other.getA()
            );
        }

        public Vector getInverted() {return new Vector(-getA(),-getB(),-getC());}

        public Vector getScaled(double scale) {return new Vector(getA()*scale, getB()*scale, getC()*scale);}

        public Vector getRounded() {return new Vector(roundDouble.apply(getA()), roundDouble.apply(getB()), roundDouble.apply(getC()));}

        // divide vector by its length, nan case returns 0,0,0
        public Vector getUnitVector() {return (getLength()!=0)?new Vector(getA()/getLength(), getB()/getLength(), getC()/getLength()):new Vector(0.0,0.0,0.0);}

        // use vector deltas to get the next point from input point
        public Geom3D.P3D getEndPoint(Geom3D.P3D origin) {return new Geom3D.P3D(getA() + origin.getA(), getB() + origin.getB(), getC() + origin.getC());}
    }

    // pair of two values, something between a hashMap and just 2 values
    public static class Pair<E, T> {
        private final E a;
        private final T b;

        public Pair(E a, T b) {
            this.a = a;
            this.b = b;
        }

        public E getA() {
            return a;
        }

        public T getB() {
            return b;
        }

        @Override
        public String toString() {
            return a + " " + b;
        }
    }

    // tuple of values
    public static class Tuple<E> {
        private final E a, b;

        public Tuple(E a, E b) {
            this.a = a;
            this.b = b;
        }

        public E getA() {
            return a;
        }

        public E getB() {
            return b;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Tuple) {
                if (((Tuple<?>) o).a.getClass() == a.getClass()) {
                    return ((((Tuple<?>) o).a == a) && (((Tuple<?>) o).b == b)) || ((((Tuple<?>) o).b == a) && (((Tuple<?>) o).a == b));
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return a + " " + b;
        }
    }

    // triple of values
    public static class Triple<E> {
        private E a, b, c;

        public Triple(E a, E b, E c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public void affectAll(Consumer<E> action) {
            action.accept(a);
            action.accept(b);
            action.accept(c);
        }

        public ArrayList<E> getAsList() {
            ArrayList<E> res = new ArrayList<>();
            res.add(a);
            res.add(b);
            res.add(c);

            return res;
        }

        public void setABC(E a, E b, E c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public E getA() {
            return a;
        }

        public E getB() {
            return b;
        }

        public E getC() {
            return c;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Triple) {
                if (((Triple<?>) o).a.getClass() == a.getClass()) {
                    return (((Triple<?>) o).a.equals(a) && ((Triple<?>) o).b.equals(b) && ((Triple<?>) o).c.equals(c));
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return a + ", " + b + ", " + c;
        }
    }
}
