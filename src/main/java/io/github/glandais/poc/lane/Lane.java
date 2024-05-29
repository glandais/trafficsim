package io.github.glandais.poc.lane;

import io.github.glandais.poc.Car;
import io.github.glandais.poc.Point;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Getter
public abstract class Lane {

    static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    final long id = ID_GENERATOR.getAndIncrement();

    Map<Long, Car> cars = new HashMap<>();

    final double maxSpeed;

    final List<Lane> neighborhood;

    final List<Lane> nextLanes;

    public abstract Point getCoords(double abs);

    public abstract double getLength();

    public void addCar(Car car) {
        this.cars.put(car.getId(), car);
    }

}
