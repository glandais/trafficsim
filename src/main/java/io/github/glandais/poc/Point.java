package io.github.glandais.poc;

public record Point(double x, double y) {
    public double distance(Point to) {
        return Math.hypot(x - to.x, y - to.y);
    }

    public Point plus(Point v, double l) {
        return new Point(x + v.x * l, y + v.y * l);
    }

    public Point perp() {
        return new Point(y, -x);
    }
}
