/**
 * @author Oliver on 1/1/2018
 */
class Vec2 {
    double x;
    double y;

    Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    Vec2() {
        this(0, 0);
    }

    public static double distanceSq(double x1, double x2, double y1, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }
}