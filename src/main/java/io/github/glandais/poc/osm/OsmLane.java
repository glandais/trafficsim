package io.github.glandais.poc.osm;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import io.github.glandais.poc.Point;
import io.github.glandais.poc.lane.PointsLane;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Getter
public class OsmLane extends PointsLane {

    private final SubWay subWay;
    private final Node first;
    private final Node last;

    private static double getMaxSpeed(SubWay subWay) {
        String maxSpeedStr = subWay.parent().tags().get("maxSpeed");
        double maxSpeed = 50.0;
        if (maxSpeedStr != null) {
            maxSpeed = Double.parseDouble(maxSpeedStr);
        }
        return maxSpeed;
    }

    private static List<Point> getPoints(SubWay subWay, boolean reversed, Function<OsmNode, Point> projector) {
        List<Point> points = subWay.nodes()
                .stream()
                .map(Node::osmNode)
                .map(projector)
                .toList();
        if (reversed) {
            return points.reversed();
        } else {
            return points;
        }
    }

    public OsmLane(SubWay subWay, boolean reversed, Function<OsmNode, Point> projector) {
        super(getMaxSpeed(subWay), getPoints(subWay, reversed, projector));
        this.subWay = subWay;
        if (!reversed) {
            this.first = subWay.nodes().getFirst();
            this.last = subWay.nodes().getLast();
        } else {
            this.first = subWay.nodes().getLast();
            this.last = subWay.nodes().getFirst();
        }
    }

}
