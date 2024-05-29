package io.github.glandais.poc.lane;

import io.github.glandais.poc.Point;
import lombok.Getter;

import java.util.List;

@Getter
public class StraigthLane extends Lane {

    private final Point from;
    private final Point to;
    private final double length;

    public StraigthLane(double maxSpeed, List<Lane> neighborhood, List<Lane> nextLanes,
                        Point from, Point to) {
        super(maxSpeed, neighborhood, nextLanes);
        this.from = from;
        this.to = to;
        this.length = Math.hypot(from.x() - to.x(), from.y() - to.y());
    }

    public Point getCoords(double abs) {
        double c = abs / this.length;
        return new Point(
                from.x() + c * (to.x() - from.x()),
                from.y() + c * (to.y() - from.y())
        );
    }

}
