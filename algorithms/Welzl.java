package algorithms;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class Welzl {
    public static Circle RunWelzl(List<Point> points) {
        List<Point> Shuffled = new ArrayList<>(points);
        Collections.shuffle(Shuffled, new Random());
        Circle circle = null;
        for (int i = 0; i < Shuffled.size(); i++) {
            Point p = Shuffled.get(i);
            if (circle == null || !circle.contains(p))
                circle = CircleUsingOnePoint(Shuffled.subList(0, i + 1), p);
        }
        return circle;
    }

    public static Circle CircleUsingOnePoint(List<Point> points, Point p) {
        Circle circle = new Circle(p, 0);
        for (int i = 0; i < points.size(); i++) {
            Point q = points.get(i);
            if (!circle.contains(q)) {
                if (circle.radius == 0)
                    circle = Diameter(p, q);
                else
                    circle = CircleUsingTwoPoints(points.subList(0, i + 1), p, q);
            }
        }
        return circle;
    }

    public static Circle CircleUsingTwoPoints(List<Point> points, Point p, Point q) {
        Circle DiameterCircle = Diameter(p, q);
        Circle LCircle  = null;
        Circle RCircle = null;
        Point NewP = q.Reduce(p);
        for (Point point : points) {
            if (DiameterCircle.contains(point))
                continue;
            double CrossedPoint = NewP.CrossPoints(point.Reduce(p));
            Circle circle = ScopeCircle(p, q, point);
            if (CrossedPoint > 0 && (LCircle == null || NewP.CrossPoints(circle.center.Reduce(p)) > NewP.CrossPoints(LCircle.center.Reduce(p))))
                LCircle = circle;
            else if (CrossedPoint < 0 && (RCircle == null || NewP.CrossPoints(circle.center.Reduce(p)) < NewP.CrossPoints(RCircle.center.Reduce(p))))
                RCircle = circle;
        }
        if (LCircle == null && RCircle == null)
            return DiameterCircle;
        else if (RCircle == null)
            return LCircle;
        else if (LCircle == null)
            return RCircle;
        else
            return LCircle.radius <= RCircle.radius ? LCircle : RCircle;
    }

    public static Circle ScopeCircle(Point p1, Point p2, Point p3) {
        float AvgX = (Math.min(Math.min(p1.x, p2.x), p3.x) + Math.max(Math.max(p1.x, p2.x), p3.x)) / 2;
        float AvgY = (Math.min(Math.min(p1.y, p2.y), p3.y) + Math.max(Math.max(p1.y, p2.y), p3.y)) / 2;
        float DisP1X = p1.x - AvgX,  DisP1Y = p1.y - AvgY,  DisP2X = p2.x - AvgX,  DisP2Y = p2.y - AvgY,DisP3X = p3.x - AvgX,  DisP3Y = p3.y - AvgY;
        float d = (DisP1X * (DisP2Y - DisP3Y) + DisP2X * (DisP3Y - DisP1Y) + DisP3X * (DisP1Y - DisP2Y)) * 2;
        if (d == 0)
            return null;
        float x = ((DisP1X*DisP1X + DisP1Y*DisP1Y) * (DisP2Y - DisP3Y) + (DisP2X*DisP2X + DisP2Y*DisP2Y) * (DisP3Y - DisP1Y) + (DisP3X*DisP3X + DisP3Y*DisP3Y) * (DisP1Y - DisP2Y)) / d;
        float y = ((DisP1X*DisP1X + DisP1Y*DisP1Y) * (DisP3X - DisP2X) + (DisP2X*DisP2X + DisP2Y*DisP2Y) * (DisP1X - DisP3X) + (DisP3X*DisP3X + DisP3Y*DisP3Y) * (DisP2X - DisP1X)) / d;
        Point p = new Point(AvgX + x, AvgY + y);
        double r = Math.max(Math.max(p.distance(p1), p.distance(p2)), p.distance(p3));
        return new Circle(p, r);
    }

    public static Circle Diameter(Point a, Point b) {
        Point c = new Point((a.x + b.x) / 2, (a.y + b.y) / 2);
        return new Circle(c, Math.max(c.distance(a), c.distance(b)));
    }
}

