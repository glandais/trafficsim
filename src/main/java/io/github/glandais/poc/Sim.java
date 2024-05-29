package io.github.glandais.poc;

import io.github.glandais.poc.interchange.Interchange;
import io.github.glandais.poc.lane.CircleLane;
import io.github.glandais.poc.lane.Lane;
import io.github.glandais.poc.lane.StraigthLane;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Getter
public class Sim {

    final Random r = new SecureRandom();

    List<Lane> lanes = new ArrayList<>();

    final List<Car> cars = new ArrayList<>();

    public Sim() {

        lanes.addAll(Interchange.getInterchange());

        // createNetwork1();

        double totlength = 0.0;
        for (Lane lane : lanes) {
            totlength = totlength + lane.getLength();
        }

        int c = 500;
        for (int i = 0; i < c; i++) {
            double l = r.nextDouble(totlength);
            double t = 0.0;
            for (int j = 0; j < lanes.size(); j++) {
                t = t + lanes.get(j).getLength();
                if (l <= t) {
                    double abs = t - l;
                    double speed = 0;//lanes.get(j).getMaxSpeed();// * (0.8 + r.nextDouble(0.2));
                    double maxSpeedCoef = 0.8 + r.nextDouble(0.4);
                    cars.add(new Car(lanes.get(j), abs, speed, maxSpeedCoef));
                    break;
                }
            }

        }
    }

    private void createNetwork1() {
        //        Lane lane0 = new StraigthLane(130,
//                new ArrayList<>(),
//                new ArrayList<>(),
//                new Point(50, 700),
//                new Point(500, 700));

        Lane lane1 = new CircleLane(130,
                new Point(500, 400),
                300,
                -Math.PI / 2,
                -3 * Math.PI / 2);

        Lane lane2 = new StraigthLane(130,
                new Point(500, 700),
                new Point(800, 700));

        Lane lane3 = new CircleLane(130,
                new Point(800, 400),
                300,
                Math.PI / 2,
                -Math.PI / 2);

        Lane lane4 = new StraigthLane(130,
                new Point(800, 100),
                new Point(500, 100));

        Lane lane5 = new CircleLane(130,
                new Point(800, 600),
                100,
                Math.PI / 2,
                -Math.PI / 2);
        Lane lane6 = new CircleLane(130,
                new Point(800, 400),
                100,
                Math.PI / 2,
                3 * Math.PI / 2);
        Lane lane7 = new CircleLane(130,
                new Point(800, 200),
                100,
                Math.PI / 2,
                -Math.PI / 2);

        Lane lane8 = new CircleLane(130,
                new Point(500, 200),
                100,
                -Math.PI / 2,
                -3 * Math.PI / 2);
        Lane lane9 = new CircleLane(130,
                new Point(500, 400),
                100,
                -Math.PI / 2,
                Math.PI / 2);
        Lane lane10 = new CircleLane(130,
                new Point(500, 600),
                100,
                -Math.PI / 2,
                -3 * Math.PI / 2);

//        lane0.getNextLanes().add(lane2);
        lane1.getNextLanes().add(lane2);
        lane2.getNextLanes().add(lane3);
        lane3.getNextLanes().add(lane4);
        lane4.getNextLanes().add(lane1);

        lane2.getNextLanes().add(lane5);
        lane5.getNextLanes().add(lane6);
        lane6.getNextLanes().add(lane7);
        lane7.getNextLanes().add(lane4);

        lane4.getNextLanes().add(lane8);
        lane8.getNextLanes().add(lane9);
        lane9.getNextLanes().add(lane10);
        lane10.getNextLanes().add(lane2);

//        lanes.add(lane0);
        lanes.add(lane1);
        lanes.add(lane2);
        lanes.add(lane3);
        lanes.add(lane4);
        lanes.add(lane5);
        lanes.add(lane6);
        lanes.add(lane7);
        lanes.add(lane8);
        lanes.add(lane9);
        lanes.add(lane10);
    }

    double ellapsed = 0.0;
    int previousSeconds = 0;

    long nanos = 0;
    long runs = 0;

    public void update(double seconds) {
        ellapsed = ellapsed + seconds;
        if (ellapsed > previousSeconds) {
//            Car car = new Car(lanes.get(0), 0, 0, 0.8 + r.nextDouble(0.4));
//            cars.add(car);
            previousSeconds = 5 + (int) Math.floor(ellapsed);
        }

        synchronized (cars) {
            long now = System.nanoTime();
            cars.stream()
//                    .parallel()
                    .forEach(car -> car.update(seconds));
            nanos = nanos + (System.nanoTime() - now);
        }
        runs++;

        if (runs % 1000 == 0) {
            System.out.println(nanos / (runs * 1000000.0));
        }
    }

    private <T> List<T> mapCars(Function<Car, T> map) {
        synchronized (cars) {
            return cars.stream()
                    .parallel()
                    .map(map)
                    .toList();
        }
    }


    public List<CarPos> getCarPositions() {
        return mapCars(car -> new CarPos(car.getId(), car.getSpeed(), car.getPos()));
    }

    public void run(double total, double delta) {
        double elapsed = 0.0;
        while (elapsed < total) {
            update(delta);
            elapsed = elapsed + delta;
//            System.out.println(
//                    cars.stream()
//                            .map(c -> "" + Math.round(c.getMainLaneAbs()) + "-" + Math.round(c.getSpeed()))
//                            .collect(Collectors.joining(" "))
//            );
        }
    }
}
