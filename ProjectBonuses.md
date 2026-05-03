# Project Bonuses

## Stage 2

1. **CylinderTests**
   Added a dedicated test class for the `Cylinder` geometry instead of relying only on inherited behavior from `Tube`.

2. **`getNormal(Point)` test for `Cylinder`**
   Added a focused test for `Cylinder.getNormal(Point)` with clear Equivalence Partition and Boundary Value coverage.

3. **`getNormal(Point)` implementation in `Cylinder`**
   Added the actual `getNormal(Point)` method in `Cylinder`, as required by the geometry hierarchy, so the finite cylinder now handles side and base normals directly.

## Stage 3

1. **Polygon ray intersections (1 pt)**
   Added implementation and tests for ray intersections with `Polygon`, including inside, outside, edge, vertex, and edge-continuation cases.

2. **Triangle ray intersections with Moller-Trumbore (1 pt)**
   Implemented `Triangle.findIntersections(Ray)` using the Moller-Trumbore algorithm and added tests for inside, outside, edge, vertex, and continuation scenarios.

3. **Tube ray intersections (2 pts)**
   Added implementation and broad test coverage for `Tube.findIntersections(Ray)`, including ray positions relative to the tube and axis, angle categories (parallel, orthogonal, acute, obtuse), tangent cases, and 0/1/2 intersection outcomes.

4. **Cylinder ray intersections (2 pts)**
   Added `Cylinder.findIntersections(Ray)` for the finite cylinder, including shell hits, bottom and top cap hits, seam points between shell and bases, duplicate filtering, and ordered results along the ray.

5. **Cylinder intersection tests**
   Added dedicated `Cylinder` intersection tests covering cap-only cases, shell-only cases, mixed shell-and-cap cases, seam hits, tangent cases, axis-parallel rays, orthogonal rays, and angled rays.
