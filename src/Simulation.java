import java.util.*;

/**
 * @author Oliver on 1/1/2018
 */
public class Simulation {
    private double G = 0.3;
    private double SOFTENING = 7;
    private double THETA = 0.5;
    private double TIME_STEP = 0.03;

    private Random rng = new Random(0);

    private List<Double> px;
    private List<Double> py;
    private List<Double> vx;
    private List<Double> vy;
    private List<Double> mass;

    private long frame = 0;

    public Simulation() {
        px = new ArrayList<>();
        py = new ArrayList<>();
        vx = new ArrayList<>();
        vy = new ArrayList<>();
        mass = new ArrayList<>();
    }

    public static Simulation fromInitialState(List<Double> px, List<Double> py, List<Double> vx, List<Double> vy, List<Double> mass) {
        Simulation sim = new Simulation();
        sim.px.addAll(px);
        sim.py.addAll(py);
        sim.vx.addAll(vx);
        sim.vy.addAll(vy);
        sim.mass.addAll(mass);
        return sim;
    }

    /**
     * Create a new galaxy Simulation with a Gaussian density distribution,
     * including a black hole as massive as the whole galaxy.
     * Initial velocities will be initialized such that the galaxy is in orbit.
     *
     * @param N      the number of bodies.
     * @param cx     center x.
     * @param cy     center y.
     * @param vx     velocity x.
     * @param vy     velocity y.
     * @param stdDev standard deviation of the bodies form the center.
     */
    public Simulation createGaussianGalaxy(int N, double cx, double cy, double vx, double vy, double stdDev) {
        Random rng = new Random(0);
        Simulation galaxy = new Simulation();
        galaxy.addBody(cx, cy, vx, vy, N);
        for (int i = 1; i < N; ) {
            double x = rng.nextGaussian() * stdDev;
            double y = rng.nextGaussian() * stdDev;
            if (x * x + y * y > 100) {
                galaxy.addBody(cx + x, cy + y, 0, 0, 1);
                i++;
            }
        }
        BHTree bHTree = BHTree.fromState(galaxy.px, galaxy.py, galaxy.mass);
        for (int i = 0; i < N; i++) {
            Vec2 a = getGravAccelHelper(bHTree.quadTree, bHTree.size(), galaxy.px.get(i), galaxy.py.get(i));
            double dx = cx - galaxy.px.get(i);
            double dy = cy - galaxy.py.get(i);
            double r = Math.sqrt(dx * dx + dy * dy);
            double aMag = Math.sqrt(a.x * a.x + a.y * a.y);
            double velMag = Math.sqrt(aMag * r);
            galaxy.vx.set(i, a.y / aMag * velMag + vx);
            galaxy.vy.set(i, -a.x / aMag * velMag + vy);
        }
        return galaxy;
    }

    /**
     * Uses the leapfrog integration method to step the simulation forward one {@code TIME_STEP}.
     */
    public void advance() {
        for (int i = 0; i < size(); i++) {
            px.set(i, px.get(i) + vx.get(i) * TIME_STEP / 2);
            py.set(i, py.get(i) + vy.get(i) * TIME_STEP / 2);
        }
        for (int i = 0; i < size(); i++) {
            double ax = 0;
            double ay = 0;
            for (int j = 0; j < size(); j++) {
                if (i != j) {
                    Vec2 gravity = getGravAccel(i, j);
                    ax += gravity.x;
                    ay += gravity.y;
                }
            }
            vx.set(i, vx.get(i) + ax * TIME_STEP);
            vy.set(i, vy.get(i) + ay * TIME_STEP);
        }
        for (int i = 0; i < size(); i++) {
            px.set(i, px.get(i) + vx.get(i) * TIME_STEP / 2);
            py.set(i, py.get(i) + vy.get(i) * TIME_STEP / 2);
        }
        frame += 1;
    }

    public void advanceBH() {
        for (int i = 0; i < size(); i++) {
            px.set(i, px.get(i) + vx.get(i) * TIME_STEP / 2);
            py.set(i, py.get(i) + vy.get(i) * TIME_STEP / 2);
        }
        BHTree bHTree = BHTree.fromState(px, py, mass);
        for (int i = 0; i < size(); i++) {
            Vec2 a = getGravAccel(bHTree, i);
            vx.set(i, vx.get(i) + a.x * TIME_STEP);
            vy.set(i, vy.get(i) + a.y * TIME_STEP);
        }
        for (int i = 0; i < size(); i++) {
            px.set(i, px.get(i) + vx.get(i) * TIME_STEP / 2);
            py.set(i, py.get(i) + vy.get(i) * TIME_STEP / 2);
        }
        frame += 1;
    }

    public void advanceEuler() {
        for (int i = 0; i < size(); i++) {
            double ax = 0;
            double ay = 0;
            for (int j = 0; j < size(); j++) {
                if (i != j) {
                    double dx = px.get(j) - px.get(i);
                    double dy = py.get(j) - py.get(i);
                    double r = distance(i, j);
                    double a = G * mass.get(j) / (r * r + SOFTENING * SOFTENING);
                    ax += a / r * dx;
                    ay += a / r * dy;
                }
            }
            vx.set(i, vx.get(i) + ax * TIME_STEP);
            vy.set(i, vy.get(i) + ay * TIME_STEP);
        }
        for (int i = 0; i < size(); i++) {
            px.set(i, px.get(i) + vx.get(i) * TIME_STEP);
            py.set(i, py.get(i) + vy.get(i) * TIME_STEP);
        }
        frame += 1;
    }

    private Vec2 getGravAccel(int i, int j) {
        return getGravAccel(px.get(i), py.get(i), px.get(j), py.get(j), mass.get(j));
    }

    private Vec2 getGravAccel(double x1, double y1, double x2, double y2, double mass2) {
        if (x1 == x2 && y1 == y2) {
            return new Vec2();  // For safety.
        } else {
            double dx = x2 - x1;
            double dy = y2 - y1;
            double r = Math.sqrt(dx * dx + dy * dy);
            double a = G * mass2 / (r * r + SOFTENING * SOFTENING);
            return new Vec2(a / r * dx, a / r * dy);
        }
    }

    private Vec2 getGravAccel(BHTree bHTree, int i) {
        return getGravAccelHelper(bHTree.quadTree, bHTree.size(), px.get(i), py.get(i));
    }

    private Vec2 getGravAccelHelper(QuadTree quadTree, double size, double x, double y) {
        if (quadTree == null) {
            return new Vec2();
        }
        if (quadTree.isTerminal()) {
            // compute acceleration based on force between this terminal node and given body.
            return getGravAccel(x, y, quadTree.cmX, quadTree.cmY, quadTree.mass);
        } else {
            if ((size * size) / Vec2.distanceSq(quadTree.cmX, x, quadTree.cmY, y) < THETA) {
                // treat the center of mass represented by this node as a single body.
                return getGravAccel(x, y, quadTree.cmX, quadTree.cmY, quadTree.mass);
            } else {
                // do for each of the children.
                Vec2 q1A = getGravAccelHelper(quadTree.q1, size / 2, x, y);
                Vec2 q2A = getGravAccelHelper(quadTree.q2, size / 2, x, y);
                Vec2 q3A = getGravAccelHelper(quadTree.q3, size / 2, x, y);
                Vec2 q4A = getGravAccelHelper(quadTree.q4, size / 2, x, y);
                return new Vec2(q1A.x + q2A.x + q3A.x + q4A.x, q1A.y + q2A.y + q3A.y + q4A.y);
            }
        }
    }

    public void addBody(double px, double py, double vx, double vy, double mass) {
        this.px.add(px);
        this.py.add(py);
        this.vx.add(vx);
        this.vy.add(vy);
        this.mass.add(mass);
    }

    public void addBodies(Simulation sim) {
        for (int i = 0; i < sim.size(); i++) {
            addBody(sim.px.get(i), sim.py.get(i), sim.vx.get(i), sim.vy.get(i), sim.mass.get(i));
        }
    }

    public ArrayList<Double> getPx() {
        return new ArrayList<>(px);
    }

    public ArrayList<Double> getPy() {
        return new ArrayList<>(py);
    }

    public ArrayList<Double> getVx() {
        return new ArrayList<>(vx);
    }

    public ArrayList<Double> getVy() {
        return new ArrayList<>(vy);
    }

    public long getFrame() {
        return frame;
    }

    public int size() {
        return px.size();
    }

    private double distance(int i, int j) {
        double dx = px.get(i) - px.get(j);
        double dy = py.get(i) - py.get(j);
        return Math.sqrt(dx * dx + dy * dy);
    }
}