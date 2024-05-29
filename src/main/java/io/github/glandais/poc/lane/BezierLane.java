package io.github.glandais.poc.lane;

import io.github.glandais.poc.Point;
import lombok.Getter;

@Getter
public class BezierLane extends Lane {

    private static final double TOLERANCE = 1e-5;

    private final double length;
    private final Point p0;
    private final Point p1;
    private final Point p2;
    private final Point p3;

    public BezierLane(double maxSpeed, Point p0, Point p1, Point p2, Point p3) {
        super(maxSpeed);
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.length = computeLength();
    }

    private double computeLength() {
        return computeLength(0, 1);
    }

    private double computeLength(double s, double e) {
        Point ps = getPoint(s);
        double m = s + (e - s) / 2.0;
        Point pm = getPoint(m);
        Point pe = getPoint(e);
        double dsm = ps.distance(pm);
        double dme = pm.distance(pe);
        double dse = ps.distance(pe);
        if (dsm + dme - dse < TOLERANCE) {
            return dsm + dme;
        } else {
            return computeLength(s, m) + computeLength(m, e);
        }
    }

    @Override
    public Point getCoords(double abs) {
        double t = abs / length;
        return getPoint(t);
    }

    private Point getPoint(double t) {
        double x = Math.pow(1 - t, 3) * p0.x() + 3 * t * Math.pow(1 - t, 2) * p1.x() + 3 * t * t * (1 - t) * p2.x()
                + Math.pow(t, 3) * p3.x();
        double y = Math.pow(1 - t, 3) * p0.y() + 3 * t * Math.pow(1 - t, 2) * p1.y() + 3 * t * t * (1 - t) * p2.y()
                + Math.pow(t, 3) * p3.y();
        return new Point(x, y);
    }

}
