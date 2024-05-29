package io.github.glandais.poc;

import io.github.glandais.poc.lane.Lane;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class Car {

    static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    final long id = ID_GENERATOR.getAndIncrement();

    static final Random R = new SecureRandom();

    final double maxSpeedCoef;

    Lane mainLane;
    double mainLaneAbs;
    Point pos;
    double speed;
    Lane toLand;
    double toLandCoef;

    public Car(Lane mainLane, double mainLaneAbs, double speed, double maxSpeedCoef) {
        this.mainLane = mainLane;
        this.mainLane.addCar(this);
        setMainLaneAbs(mainLaneAbs);
        this.speed = speed;
        this.maxSpeedCoef = maxSpeedCoef;
    }

    public void setMainLaneAbs(double mainLaneAbs) {
        double overflow = mainLaneAbs;
        while (overflow > mainLane.getLength()) {
            overflow = overflow - mainLane.getLength();
            this.mainLane.getCars().remove(this.getId());
            this.mainLane = this.mainLane.getNextLanes().get(R.nextInt(this.mainLane.getNextLanes().size()));
            this.mainLane.getCars().put(this.getId(), this);
        }
        this.mainLaneAbs = overflow;
        this.pos = this.mainLane.getCoords(this.mainLaneAbs);
    }

    public void update(double seconds) {
        double a = 0;
        if (speed < mainLane.getMaxSpeed() * maxSpeedCoef * 0.95) {
            a = 5;
        } else if (speed > mainLane.getMaxSpeed() * maxSpeedCoef * 1.05) {
            a = -10;
        }
        double distStop = getDistStop(2.0, this);
        boolean isFrontCarForcingBrake = isFrontCarForcingBrake(distStop);
        if (isFrontCarForcingBrake) {
            a = -10;
        }
        speed = speed + seconds * a;
        setMainLaneAbs(mainLaneAbs + (speed * 1000.0 / 3600.0) * seconds);
    }

    private boolean isFrontCarForcingBrake(double distStop) {
        Car frontCarForcingBrake = isFrontCarForcingBrake(mainLane, mainLaneAbs, distStop);
        return frontCarForcingBrake != null;
    }

    private Car isFrontCarForcingBrake(Lane lane, double laneAbs, double distStop) {
        Car result = null;
        double absStop = laneAbs + distStop;
        for (Car frontCar : lane.getCars().values()) {
            if (result == null &&
                    frontCar.getMainLaneAbs() > laneAbs &&
                    frontCar.getMainLaneAbs() < absStop) {
                double distStopFrontCar = getDistStop(0.0, frontCar);
                double frontCarAbsStop = frontCar.getMainLaneAbs() + distStopFrontCar;
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
        double s = car.getSpeed();
        double d = 0.1;
        while (s > 0) {
            s = s + d * -10;
            dist = dist + d * (s * 1000.0 / 3600.0);
        }
        return dist + 2.0;
    }

}
