package io.github.glandais.poc.lane;

import io.github.glandais.poc.Point;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleDrawableLine extends Lane {

    public SimpleDrawableLine(double maxSpeed) {
        super(maxSpeed);
    }

    @Override
    public List<Point> getPoints() {
        int c = 1 + (int) (getLength() / 10.0);
        double da = getLength() / c;
        List<Point> result = new ArrayList<>();
        for (int i = 0; i < c; i++) {
            double sa = da * i;
            result.add(getCoords(sa));
        }
        return result;
    }

}
