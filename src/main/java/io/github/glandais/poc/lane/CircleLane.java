package io.github.glandais.poc.lane;

import io.github.glandais.poc.Point;
import lombok.Getter;

import java.util.List;

@Getter
public class CircleLane extends Lane {

    private final double length;
    private final Point center;
    private final double radius;
    private final double angleStart;
    private final double angleEnd;

    public CircleLane(double maxSpeed,
                      Point center, double radius, double angleStart, double angleEnd) {
        super(maxSpeed);
        this.center = center;
        this.radius = radius;
        this.angleStart = angleStart;
        this.angleEnd = angleEnd;
        this.length = this.radius * Math.abs(angleEnd - angleStart);
    }

    public Point getCoords(double abs) {
        double angle = angleStart + (angleEnd - angleStart) * abs / this.length;
        return new Point(center.x() + radius * Math.cos(angle), center.y() + radius * Math.sin(angle));
    }

}
