package io.github.glandais.poc.lane;

import io.github.glandais.poc.Point;

import java.util.List;

public abstract class PointsLane extends Lane {
    private final List<Point> points;
    private double length;

    public PointsLane(double maxSpeed, List<Point> points) {
        super(maxSpeed);
        this.points = points;
        this.length = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            this.length = this.length + points.get(i).distance(points.get(i + 1));
        }
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public Point getCoords(double abs) {
        double curAbs = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            double d = p1.distance(p2);
            if (curAbs + d > abs) {
                double a = (abs - curAbs) / d;
                return new Point(
                        p1.x() + a * (p2.x() - p1.x()),
                        p1.y() + a * (p2.y() - p1.y())
                );
            }
            curAbs = curAbs + d;
        }
        return points.getLast();
    }

    @Override
    public List<Point> getPoints() {
        return points;
    }
}
