package algorithms;

public class Point {
	public final float x,y;
	public Point(float x, float y) {
		this.x=x;
		this.y=y;
	}

	public Point Reduce(Point p) {return new Point(x - p.x, y - p.y);}

	public double CrossPoints(Point p) {return x * p.y - y * p.x;}

	public double distance(Point p) {return Math.hypot(x - p.x, y - p.y);}

	public double distanceSquaredTo(final Point p) {
		final double DX = x - p.x;
		final double DY = y - p.y;
		return DX * DX + DY * DY;
	}

	@Override
	public String toString() {
		return "X: " + x + ", Y: " + y;
	}
}
