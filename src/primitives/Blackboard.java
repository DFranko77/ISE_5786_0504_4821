package primitives;

import static primitives.Util.isZero;

import java.util.ArrayList;
import java.util.List;

/**
 * A 2D sampling target placed in 3D space: given a flat area (a disk or a
 * square) positioned and oriented in the scene, it produces a set of sample
 * points that cover that area as uniformly as the chosen pattern allows.
 *
 * <p>The class is a pure geometric service — it depends only on {@link Point}
 * and {@link Vector}. The <em>pattern</em> (how samples are distributed inside
 * the area) is stored as cached 2D offsets and is independent of the area's
 * placement, so the same offsets can be re-mapped cheaply every time the area
 * is moved or re-oriented. This makes it reusable for area-light soft shadows
 * today and for camera depth-of-field / anti-aliasing later.</p>
 */
public class Blackboard {

   /** Target-area outline. */
   public enum Shape {
      /** Circular area (default) — isotropic penumbra, no axis-aligned banding. */
      CIRCLE,
      /** Square area — used by anti-aliasing, which samples a square pixel. */
      SQUARE
   }

   /** Distribution of the samples inside the area. */
   public enum Pattern {
      /** Deterministic cell centers — reproducible, good for unit tests. */
      GRID,
      /** One random sample per grid cell (default) — uniform yet banding-free. */
      JITTERED,
      /** Pure uniform-random samples — simplest, but noisier for the same count. */
      STOCHASTIC
   }

   /** A single 2D sample offset within the area, in {@code [-size, +size]}. */
   public record Sample(double u, double v) {}

   /** Circle radius / square half-edge. */
   private double _size = 1d;
   /** Number of cells per axis (total samples ≈ {@code density²}, fewer for a circle). */
   private int _density = 1;
   /** Area outline. */
   private Shape _shape = Shape.CIRCLE;
   /** Sample distribution. */
   private Pattern _pattern = Pattern.JITTERED;

   /** Where the area sits in space (set by {@link #orient}). */
   private Point _center = Point.ZERO;
   /** Local orthonormal basis spanning the area's plane (set by {@link #orient}). */
   private Vector _vRight = Vector.AXIS_X;
   /** Local orthonormal basis spanning the area's plane (set by {@link #orient}). */
   private Vector _vUp = Vector.AXIS_Y;

   /** Cached 2D offsets; rebuilt only when a shape/size/pattern parameter changes. */
   private List<Sample> _offsets;
   /** True when {@link #_offsets} must be regenerated. */
   private boolean _dirty = true;

   /**
    * Sets the area size — circle radius or square half-edge.
    *
    * @param size half-extent of the area (&gt; 0)
    * @return this blackboard for chaining
    */
   public Blackboard setSize(double size) {
      _size = size;
      _dirty = true;
      return this;
   }

   /**
    * Sets the desired number of samples. Internally stored as a per-axis
    * density of {@code ceil(sqrt(numOfRays))}; for a {@link Shape#CIRCLE} the
    * corners are then rejected, so the realized count is somewhat lower.
    *
    * @param numOfRays target number of samples (&ge; 1)
    * @return this blackboard for chaining
    */
   public Blackboard setNumOfRays(int numOfRays) {
      _density = Math.max(1, (int) Math.ceil(Math.sqrt(numOfRays)));
      _dirty = true;
      return this;
   }

   /**
    * Sets the area outline.
    *
    * @param shape {@link Shape#CIRCLE} or {@link Shape#SQUARE}
    * @return this blackboard for chaining
    */
   public Blackboard setShape(Shape shape) {
      _shape = shape;
      _dirty = true;
      return this;
   }

   /**
    * Sets the sample distribution pattern.
    *
    * @param pattern grid, jittered, or stochastic distribution
    * @return this blackboard for chaining
    */
   public Blackboard setPattern(Pattern pattern) {
      _pattern = pattern;
      _dirty = true;
      return this;
   }

   /**
    * Places the area in space, building a local orthonormal basis whose plane is
    * orthogonal to {@code normal}. Placement does not invalidate the cached 2D
    * offsets — that is the whole point of storing offsets rather than points.
    *
    * @param center where the area sits in space
    * @param normal vector orthogonal to the area's plane (need not be a unit vector)
    * @return this blackboard for chaining
    */
   public Blackboard orient(Point center, Vector normal) {
      _center = center;
      Vector d = normal.normalize();
      // Pick the world axis least parallel to d so the cross product is stable.
      double dx = Math.abs(d.dotProduct(Vector.AXIS_X));
      double dy = Math.abs(d.dotProduct(Vector.AXIS_Y));
      double dz = Math.abs(d.dotProduct(Vector.AXIS_Z));
      Vector axis = (dx <= dy && dx <= dz) ? Vector.AXIS_X
                  : (dy <= dz) ? Vector.AXIS_Y
                  : Vector.AXIS_Z;
      _vRight = d.crossProduct(axis).normalize();
      _vUp = d.crossProduct(_vRight).normalize();
      return this;
   }

   /**
    * Returns the sample points mapped into 3D, regenerating the cached 2D offset
    * pattern first if any parameter changed.
    *
    * @return list of sample points covering the oriented area
    */
   public List<Point> points() {
      if (_dirty) generate();
      List<Point> pts = new ArrayList<>(_offsets.size());
      for (Sample s : _offsets) {
         Point p = _center;
         if (!isZero(s.u())) p = p.add(_vRight.scale(s.u()));
         if (!isZero(s.v())) p = p.add(_vUp.scale(s.v()));
         pts.add(p);
      }
      return pts;
   }

   /** (Re)builds the cached 2D offset pattern over {@code [-size, +size]²}. */
   private void generate() {
      _offsets = new ArrayList<>(_density * _density);
      double cell = 2d * _size / _density;
      double sizeSquared = _size * _size;
      for (int i = 0; i < _density; i++) {
         for (int j = 0; j < _density; j++) {
            double u;
            double v;
            switch (_pattern) {
               case GRID -> {
                  u = -_size + (i + 0.5) * cell;
                  v = -_size + (j + 0.5) * cell;
               }
               case STOCHASTIC -> {
                  u = Util.random(-_size, _size);
                  v = Util.random(-_size, _size);
               }
               default -> { // JITTERED: one random point inside each cell
                  u = -_size + (i + Util.random(0d, 1d)) * cell;
                  v = -_size + (j + Util.random(0d, 1d)) * cell;
               }
            }
            // Disk rejection: keep only offsets inside the inscribed circle.
            if (_shape == Shape.CIRCLE && u * u + v * v > sizeSquared) continue;
            _offsets.add(new Sample(u, v));
         }
      }
      // Guarantee at least the center sample so callers never get an empty set.
      if (_offsets.isEmpty()) _offsets.add(new Sample(0d, 0d));
      _dirty = false;
   }
}
