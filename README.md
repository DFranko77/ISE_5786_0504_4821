# ISE_5786_0504_4821

Java 3D geometry, camera, and intersection library for ray-tracing course exercises.

## Overview

This project implements:

1. **Primitives**: `Point`, `Vector`, `Ray`, `Double3`, `Util`
2. **Geometry API**: `Intersectable`, `Geometry`
3. **Geometry implementations**: `Plane`, `Sphere`, `Triangle`, `Polygon`, `Tube`, `Cylinder`
4. **Composite geometry**: `Geometries` (collection of intersectables)
5. **Renderer**: `Camera` with a nested `Builder`

Core capabilities currently include:

1. Vector and point algebra
2. Ray point evaluation (`Ray#getPoint(double)`)
3. Surface normals for supported geometries
4. Ray intersection calculations for the implemented geometries
5. Camera construction through a builder-based API
6. Ray generation through the camera view plane
7. Integration coverage between camera rays and geometric intersections

## Project Structure

```text
src/
  geometries/
    api/
    impl/
  primitives/
  renderer/
  test/
unitTests/
  geometries/
    impl/
  primitives/
  renderer/
lib/
```

- `src\` contains production code.
- `unitTests\` contains JUnit 5 tests.
- `src\test\Main.java` contains basic runtime sanity checks.
- `lib\` contains local JUnit jars used by the IntelliJ module setup.

## Prerequisites

1. Java JDK (project is configured as an IntelliJ Java module)
2. IntelliJ IDEA (recommended)

## Running the Project

### Run sanity checks

Run `src\test\Main.java` as a Java application.

Expected result: if no error lines are printed, sanity checks passed.

### Run unit tests

Run all tests under `unitTests\` with JUnit 5 from IntelliJ.

The module file `ISE_5786_0504_4821.iml` is configured with:

1. `src` as source root
2. `unitTests` as test source root
3. JUnit 5 jars from `lib\`

## Current Test Coverage Areas

1. `primitives`: `Point`, `Vector`, `Ray`
2. `geometries.impl`: `Plane`, `Sphere`, `Triangle`, `Polygon`, `Tube`, `Cylinder`, `Geometries`
3. `renderer`: `Camera` builder validation, ray construction, and camera-geometry integration scenarios

## Notes

1. This repository currently uses IntelliJ module configuration instead of Maven/Gradle.
2. API documentation (Javadoc) is present across the main geometry, primitives, and camera code and can be extended as features are added.
3. The camera currently supports builder-based setup for location, orientation, view-plane dimensions, view-plane distance, resolution, and ray construction per pixel.

## Maintenance

This README is intended to stay up to date with the codebase.  
When new geometry types, features, folders, or test flows are added, update the relevant sections above.
