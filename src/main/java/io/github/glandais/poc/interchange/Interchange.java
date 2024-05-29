package io.github.glandais.poc.interchange;

import io.github.glandais.poc.Point;
import io.github.glandais.poc.lane.BezierLane;
import io.github.glandais.poc.lane.CircleLane;
import io.github.glandais.poc.lane.Lane;
import io.github.glandais.poc.lane.StraigthLane;

import java.util.ArrayList;
import java.util.List;

public class Interchange {

    protected static final Point zero = new Point(1920 / 2.0, 1080 / 2.0);
    protected static final double L = 2000;
    protected static final double DV = 20.0;

    protected static final double RX1 = 300.0;
    protected static final double RX2 = 50.0;

    protected static class InterchangeLane {
        List<Lane> lanes = new ArrayList<>();

        Point start = null;
        Point v = null;

        Point end;
        Point x1, e1, x2, e2;
        Lane x1Entrance, e1Exit, x2Entrance, e2Exit;

        double ax1, ax2;
        Point cx1, cx2;

        protected InterchangeLane(int i) {
            if (i == 0) {
                ax1 = -Math.PI;
                ax2 = -Math.PI;
                start = new Point(zero.x() + DV / 2, zero.y() - L / 2);
                v = new Point(0, 1.0);
            } else if (i == 1) {
                ax1 = -Math.PI/2;
                ax2 = -Math.PI/2;
                start = new Point(zero.x() + L / 2, zero.y() + DV / 2);
                v = new Point(-1.0, 0.0);
            } else if (i == 2) {
                ax1 = 0;
                ax2 = 0;
                start = new Point(zero.x() - DV / 2, zero.y() + L / 2);
                v = new Point(0, -1.0);
            } else if (i == 3) {
                ax1 = Math.PI/2;
                ax2 = Math.PI/2;
                start = new Point(zero.x() - L / 2, zero.y() - DV / 2);
                v = new Point(1.0, 0);
            }
            if (start == null) {
                throw new IllegalStateException();
            }

            x1 = start.plus(v, L / 2 - DV / 2 - RX1);
            cx1 = x1.plus(v.perp(), RX1);
            e1 = start.plus(v, L / 2 - DV / 2 - RX2);
            x2 = start.plus(v, L / 2 + DV / 2 + RX2);
            cx2 = x2.plus(v.perp(), RX2);
            e2 = start.plus(v, L / 2 + DV / 2 + RX1);

            end = start.plus(v, L);

            x1Entrance = new StraigthLane(130, start, x1);
            lanes.add(x1Entrance);
            lanes.add(new StraigthLane(130, x1, e1));
            e1Exit = new StraigthLane(130, e1, x2);
            x2Entrance = e1Exit;
            lanes.add(e1Exit);
            lanes.add(new StraigthLane(130, x2, e2));
            e2Exit = new StraigthLane(130, e2, end);
            lanes.add(e2Exit);

            for (int j = 0; j < lanes.size(); j++) {
                lanes.get(j).getNextLanes().add(lanes.get((j + 1) % lanes.size()));
            }
        }

        public void connectX1(InterchangeLane interchangeLane) {
            Lane x1Lane = new CircleLane(
                    100,
                    cx1,
                    RX1,
                    ax1,
                    ax1 - Math.PI / 2
            );
            lanes.add(x1Lane);
            x1Entrance.getNextLanes().add(x1Lane);
            x1Lane.getNextLanes().add(interchangeLane.e2Exit);
        }

        public void connectX2(InterchangeLane interchangeLane) {
            Lane x2Lane = new CircleLane(
                    100,
                    cx2,
                    RX2,
                    ax2,
                    ax2 - 3 * Math.PI / 2
            );
            lanes.add(x2Lane);
            x2Entrance.getNextLanes().add(x2Lane);
            x2Lane.getNextLanes().add(interchangeLane.e1Exit);
        }
    }

    public static List<Lane> getInterchange() {

        List<InterchangeLane> interchangeLanes = List.of(
                new InterchangeLane(0),
                new InterchangeLane(1),
                new InterchangeLane(2),
                new InterchangeLane(3)
        );

        for (int i = 0; i < 4; i++) {
            interchangeLanes.get(i).connectX2(interchangeLanes.get((i + 1) % 4));
            interchangeLanes.get(i).connectX1(interchangeLanes.get((i + 3) % 4));
        }

        List<Lane> result = new ArrayList<>();
        for (InterchangeLane interchangeLane : interchangeLanes) {
            result.addAll(interchangeLane.lanes);
        }
        return result;
//        int a = 600;
//        Lane lane1 = new BezierLane(130,
//                new Point(400, 600),
//                new Point(400 + a, 600),
//                new Point(200, 400 - a),
//                new Point(200, 400)
//        );
//        lane1.getNextLanes().add(lane1);
//
//        return List.of(lane1);
    }

}
