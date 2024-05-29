package io.github.glandais.poc.lane;

import io.github.glandais.poc.Point;
import lombok.Getter;

import java.util.List;

@Getter
public class StraigthLane extends Lane {

    private final Point from;
    private final Point to;
    private final double length;

    public StraigthLane(double maxSpeed, Point from, Point to) {
        super(maxSpeed);
        this.from = from;
        this.to = to;
        this.length = from.distance(to);
    }

    public Point getCoords(double abs) {
        double c = abs / this.length;
        return new Point(
                from.x() + c * (to.x() - from.x()),
                from.y() + c * (to.y() - from.y())
        );
    }

}
