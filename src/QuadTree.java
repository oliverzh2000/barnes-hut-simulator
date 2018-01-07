import java.util.Collections;
import java.util.List;

import static java.lang.Double.NaN;

/**
 * @author Oliver on 1/4/2018
 */
public class QuadTree {
    double cmX = NaN;
    double cmY = NaN;
    double mass = NaN;

    QuadTree q1;
    QuadTree q2;
    QuadTree q3;
    QuadTree q4;

    public static void main(String[] args) {
        QuadTree qt = new QuadTree();
        qt.addBody(0, 0, 100, 20, 20, 1);
        qt.addBody(0, 0, 100, 60, 20, 1);
        qt.addBody(0, 0, 100, 60, 30, 1);
        qt.addBody(0, 0, 100, 80, 30, 1);
        qt.addBody(0, 0, 100, 75, 75, 1);
    }

    void addBody(double minX, double minY, double size, double x, double y, double mass) {
        if (Double.isNaN(this.cmX) || Double.isNaN(this.cmY) || Double.isNaN(this.mass)) {
            // place new body here.
            cmX = x;
            cmY = y;
            this.mass = mass;
        } else if (!isTerminal()) {
            // update center of mass.
            // recursively insert new body into appropriate quadrant.
            updateCenterOfMass(x, y, mass);
            addBodyInCorrectQuadrant(minX, minY, size, x, y, mass);
        } else {
            // subdivide region and instantiate 4 children.
            // recursively insert current body and new body into appropriate quadrants.
            // update center of mass.
            addBodyInCorrectQuadrant(minX, minY, size, x, y, mass);
            addBodyInCorrectQuadrant(minX, minY, size, cmX, cmY, this.mass);
            updateCenterOfMass(x, y, mass);
        }
    }

    public boolean isTerminal() {
        return q1 == null && q2 == null && q3 == null && q4 == null;
    }

    private void updateCenterOfMass(double x, double y, double mass) {
        cmX = (cmX * this.mass + x * mass) / (this.mass + mass);
        cmY = (cmY * this.mass + y * mass) / (this.mass + mass);
        this.mass += mass;
    }

    private void addBodyInCorrectQuadrant(double minX, double minY, double size, double x, double y, double mass) {
        double midX = minX + size / 2;
        double midY = minY + size / 2;
        if (y < midY) {
            if (x < midX) {
                if (q1 == null) q1 = new QuadTree();
                q1.addBody(minX, minY, size / 2, x, y, mass);
            } else {
                if (q2 == null) q2 = new QuadTree();
                q2.addBody(midX, minY, size / 2, x, y, mass);
            }
        } else {
            if (x < midX) {
                if (q3 == null) q3 = new QuadTree();
                q3.addBody(minX, midY, size / 2, x, y, mass);
            } else {
                if (q4 == null) q4 = new QuadTree();
                q4.addBody(midX, midY, size / 2, x, y, mass);
            }
        }
    }
}

class BHTree {
    QuadTree quadTree;
    private final double minX;
    private final double minY;

    private final double size;

    public BHTree(double minX, double minY, double size) {
        this.minX = minX;
        this.minY = minY;
        this.size = size;
        quadTree = new QuadTree();
    }

    public static void main(String[] args) {
        BHTree bhTree = new BHTree(0, 0, 100);
        bhTree.addBody(20, 20, 1);
        bhTree.addBody(60, 20, 1);
        bhTree.addBody(60, 30, 1);
        bhTree.addBody(80, 30, 1);
        bhTree.addBody(75, 75, 1);

    }

    public static BHTree fromState(List<Double> px, List<Double> py, List<Double> mass) {
        // this may be performance heavy.
        double minX = Collections.min(px);
        double minY = Collections.min(py);
        double maxX = Collections.max(px);
        double maxY = Collections.max(py);
        // inflate the bounds so that float division errors do not create an out of bounds error.
        double size = Math.abs(Math.max(maxX - minX, maxY - minY));
        BHTree bHTree = new BHTree(minX - 1, minY - 1, size + 2);
        for (int i = 0; i < px.size(); i++) {
            // inflate the bounds so that float division errors do not create an out of bounds error.
            bHTree.addBody(px.get(i), py.get(i), mass.get(i));
        }
        return bHTree;
    }

    public void addBody(double x, double y, double mass) {
        if (x < minX || y < minY || minX + size < x || minY + size < y) {
            throw new IllegalArgumentException("x or y is out of bounds");
        }
        quadTree.addBody(minX, minY, size, x, y, mass);
    }

    public double size() {
        return size;
    }
}
