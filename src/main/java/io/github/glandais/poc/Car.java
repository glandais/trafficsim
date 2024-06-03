package io.github.glandais.poc;

import io.github.glandais.poc.lane.Lane;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class Car {

    static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    final long id = ID_GENERATOR.getAndIncrement();

    static final Random R = new SecureRandom();

    final double maxSpeedCoef;

    Lane lane;
    double laneAbs;
    Point pos;
    double speed;

    public Car(Lane lane, double laneAbs, double speed, double maxSpeedCoef) {
        this.lane = lane;
        this.lane.addCar(this);
        setLaneAbs(laneAbs);
        this.speed = speed;
        this.maxSpeedCoef = maxSpeedCoef;
    }

    public void setLaneAbs(double laneAbs) {
        double overflow = laneAbs;
        while (overflow > lane.getLength()) {
            overflow = overflow - lane.getLength();
            this.lane.getCars().remove(this.getId());
            if (!this.lane.getNextLanes().isEmpty()) {
                this.lane = this.lane.getNextLanes().get(R.nextInt(this.lane.getNextLanes().size()));
            }
            this.lane.getCars().put(this.getId(), this);
        }
        this.laneAbs = overflow;
        this.pos = this.lane.getCoords(this.laneAbs);
    }

    public void update(double seconds) {
        double a = 0;
        if (speed < lane.getMaxSpeed() * maxSpeedCoef * 0.95) {
            a = 5;
        } else if (speed > lane.getMaxSpeed() * maxSpeedCoef * 1.05) {
            a = -10;
        }
        double distStop = getDistStop(2.0, this);
        boolean isFrontCarForcingBrake = isFrontCarForcingBrake(distStop);
        if (isFrontCarForcingBrake) {
            a = -10;
        }
        speed = speed + seconds * a;
        speed = Math.max(0.0, speed);
        setLaneAbs(laneAbs + (speed * 1000.0 / 3600.0) * seconds);
    }

    private boolean isFrontCarForcingBrake(double distStop) {
        Car frontCarForcingBrake = isFrontCarForcingBrake(lane, laneAbs, distStop);
        return frontCarForcingBrake != null;
    }

    private Car isFrontCarForcingBrake(Lane lane, double laneAbs, double distStop) {
        Car result = null;
        double absStop = laneAbs + distStop;
        List<Car> laneCars;
        synchronized (lane.getCars()) {
            laneCars = new ArrayList<>(lane.getCars().values());
        }
        for (Car frontCar : laneCars) {
            if (result == null &&
                    frontCar.getLaneAbs() > laneAbs &&
                    frontCar.getLaneAbs() < absStop) {
                double distStopFrontCar = getDistStop(0.0, frontCar);
                double frontCarAbsStop = frontCar.getLaneAbs() + distStopFrontCar;
                if (absStop > frontCarAbsStop) {
                    result = frontCar;
                }
            }
        }
        if (result == null && absStop > lane.getLength()) {
            for (Lane nextLane : lane.getNextLanes()) {
                if (result == null) {
                    result = isFrontCarForcingBrake(nextLane, 0.0, distStop - (lane.getLength() - laneAbs));
                }
            }
        }
        return result;
    }

    private static double getDistStop(double reflex, Car car) {
        double dist = reflex * (car.getSpeed() * 1000.0 / 3600.0);

        return 2.0 + dist + car.speed * car.speed / (7.2 * 10);

//        double s = car.getSpeed();
//        double d = 0.1;
//        while (s > 0) {
//            s = s + d * -10;
//            dist = dist + d * (s * 1000.0 / 3600.0);
//        }
//        return dist + 2.0;
    }

}
