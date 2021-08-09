package algorithms;

public class Circle {
    public Point center;
    public double radius;
    private static final double Epsilon = 1 + 1e-14;

    public Circle(Point Center, double radius) {
        center = Center;
        this.radius = radius;
    }

    public boolean contains(Point p) {return center.distance(p) <= radius * Epsilon;}

    public boolean containsPoint(Point p) { return p.distanceSquaredTo(center) <= radius * radius; }

    @Override
    public String toString() { return center.toString()  +  ", Radius: " + radius; }
}
