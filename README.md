# barnes-hut-simulator
A 2-dimensional N body simulation in `n*log(n)` time using the Barnes-Hut binary space partitioning algorithm, written in Java.

The essence of the Barnes-Hut
algorithm is that for each star, you can treat groups of other stars (with sufficiently small angular separation) as a single star at their
combined center of mass. This greatly reduces the number of star-pair interactions at each timestep from `n^2` to `n*log(n)`, at the cost of an
acceptable amount of simulation accuracy.

Numerical integration is done using the leapfrog method. The leapfrog method ensures that the simulation is time-reversible
(to within machine precision), which implies conservation of energy via Noether's theorem.

The method `Simulation::createGaussianGalaxy(int N, double cx, double cy, double vx, double vy, double stdDev)` creates a galaxy with a given
center, velocity of the galaxy's center of mass, and N random stars normally distributed radially with standard deviation. There is a 
supermassive black hole in the center of the galaxy (for stability purposes) equal in mass to the total mass of all stars. 
The speed of each star is initialized such that at `time=0`, gravitational forces and centrifugal forces due to orbit keep the star in equilibirum. 

The demo video `galaxy_merger.mov` consists of two galaxies each with `100,000` stars orbiting each other and eventually 'merging'.
Note that in a real galaxy merger (which looks similar to this video over a much larger time scale), the probability of a head-on 
collision between two stars is virtually zero, due to the huge distances between stars. 
