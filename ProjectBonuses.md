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

## Stage 6

1. **Ambient light infrastructure**
   Added an `AmbientLight` type, scene-level ambient light support, and simple-ray-tracer integration so rendered colors can include a global ambient contribution.

2. **Material-based ambient attenuation**
   Added `Material.kA` with both scalar and `Double3` setters, so each geometry can attenuate ambient light differently, including per-channel ambient response.

3. **Geometry-aware intersection API**
   Added the `Intersectable.Intersection` hit structure and the `calcIntersections(Ray)` / `calcIntersectionsHelper(Ray)` flow, so the renderer can keep both the hit point and the intersected geometry/material together.

4. **Ambient and emission render tests**
   Added Stage 6 rendering tests for emission colors and ambient-light attenuation, including the Part II setup where different bodies use different `kA` values through their materials.

5. **External scene loading from XML/JSON**
   Added `SceneLoader` with XML and JSON parsers, shared scene-parsing utilities, sample scene files under `src\`, and focused tests that validate loaded background, ambient light, and geometry intersections.

## Stage 7

1. **Narrow-beam spotlight (1 pt)**
   Added `SpotLight.setNarrowBeam(...)` with exponent-based focus (`(dir dot L)^n`) to support flashlight/projector behavior, and enabled the enhanced-spot render tests for sphere and triangles (`testSphereSpotSharp`, `testTrianglesSpotSharp`).

## Stage 8

1. **Optimized shadow ray distance filtering (1 pt)**
   Instead of computing all intersections and filtering afterward, added a `maxDistance` parameter throughout the intersection API:
   - `Intersectable`: added `calcIntersections(Ray, double maxDistance)` public overload and replaced the single abstract `calcIntersectionsHelper(Ray)` with `calcIntersectionsHelper(Ray, double maxDistance)`.
   - All geometries (`Sphere`, `Plane`, `Triangle`, `Polygon`, `Tube`, `Cylinder`, `Geometries`): each `t` value is now guarded by `alignZero(t - maxDistance) <= 0` before being added to results.
   - Shadow rays in `transparency(...)` call `calcIntersections(shadowRay, lightDistance)` directly, so geometry beyond the light source is never computed.