# ISE_5786_0504_4821

Java ray-tracing engine built for the software-engineering ray-tracing course:
3D geometry, a camera, a Phong lighting model, recursive reflection/refraction,
shadows, and multithreaded rendering.

## Overview

This project implements:

1. **Primitives**: `Point`, `Vector`, `Ray`, `Double3`, `Util`, `Color`, `Material`, `Blackboard`
2. **Geometry API**: `Intersectable`, `Geometry`
3. **Geometry implementations**: `Plane`, `Sphere`, `Triangle`, `Polygon`, `Tube`, `Cylinder`, `RadialGeometry`
4. **Composite geometry**: `Geometries` (collection of intersectables)
5. **Lighting**: `Light`, `LightSource`, `AmbientLight`, `DirectionalLight`, `PointLight`, `SpotLight`
6. **Renderer**: `Camera` (with a nested `Builder`), `ImageWriter`, `RayTracerBase`, `SimpleRayTracer`, `RayTracerType`
7. **Scene model**: `Scene` with background color, ambient light, geometries, and light sources
8. **Scene loading**: XML/JSON parsing into `Scene` via `scene.io.SceneLoader`

Core capabilities currently include:

1. Vector and point algebra
2. Ray point evaluation (`Ray#getPoint(double)`)
3. Surface normals for supported geometries
4. Ray intersection calculations for the implemented geometries
5. Camera construction through a builder-based API
6. Ray generation through the camera view plane
7. Phong reflectance model (ambient, emission, diffuse, specular) per material
8. Multiple light sources: directional, point, and spotlight (including narrow-beam focus)
9. Shadows, including transparency-aware shadow rays through partially transparent bodies
10. Recursive global effects: reflection (`kR`) and refraction/transparency (`kT`)
11. Soft shadows via area light sources (light radius + multi-ray sampling)
12. Multithreaded rendering (parallel stream and thread-pool strategies)
13. External scene loading from XML/JSON files

## Project Structure

```text
src/
  geometries/
    api/          Intersectable, Geometry
    impl/         Plane, Sphere, Triangle, Polygon, Tube, Cylinder, RadialGeometry, Geometries
  lighting/       Light, LightSource, AmbientLight, DirectionalLight, PointLight, SpotLight
  primitives/     Point, Vector, Ray, Double3, Util, Color, Material, Blackboard
  renderer/       Camera, ImageWriter, RayTracerBase, SimpleRayTracer, RayTracerType
  scene/          Scene
    io/           SceneLoader, SceneParser, XmlSceneParser, JsonSceneParser, SceneParsingUtils
  test/           Main.java (runtime sanity checks)
unitTests/
  geometries/impl/
  primitives/
  renderer/
  scene/
  manual/         Final-picture scenes (BeachScene, BeachScene2, BeachBedScene, PalmTreeScene, MultithreadingTests)
lib/              Local JUnit jars used by the IntelliJ module setup
docs/             Design notes (e.g. soft-shadows-design.md)
images/           Rendered PNG outputs from the tests
```

- `src\` contains production code.
- `unitTests\` contains JUnit 5 tests plus the `manual\` rendered-scene drivers.
- `src\test\Main.java` contains basic runtime sanity checks.
- `lib\` contains local JUnit jars used by the IntelliJ module setup.
- `images\` holds the PNG outputs produced by the rendering tests.

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

### Render scenes

The tests under `unitTests\renderer\` and `unitTests\manual\` render images and
write them to the `images\` folder via `ImageWriter`. Run a test (for example
`RenderTests`, `LightsTests`, `ShadowTests`, `TransparencyReflectionTests`,
`SoftShadowTests`, or one of the `manual\` scenes) to regenerate the
corresponding PNG.

## Rendering Features

### Lighting and materials

- `Material` carries the ambient (`kA`), diffuse (`kD`), specular (`kS`),
  transparency (`kT`), and reflection (`kR`) coefficients plus `nShininess`.
- The Phong model combines ambient, emission, diffuse, and specular terms.
- Light sources: `DirectionalLight`, `PointLight` (with distance attenuation),
  and `SpotLight` (with optional narrow-beam focus).

### Shadows and global effects

- `SimpleRayTracer` casts shadow rays and supports recursive reflection and
  refraction up to a bounded recursion depth.
- Shadow rays are transparency-aware: partially transparent blockers attenuate
  rather than fully occlude the light, and shadow rays are distance-limited so
  geometry beyond the light source is never tested.

### Soft shadows

- A light source can be given a positive radius (`setSize`) and a sample count
  (`setNumOfRays`) to act as an area light, producing soft shadow penumbrae.
- `Blackboard` provides the geometric sampling service (target-area sampling
  with configurable shape and pattern). See `docs\soft-shadows-design.md`.

### Acceleration and performance

- `Camera` supports three render strategies via `Builder.setRenderMode(...)`:
  `SINGLE` (baseline), `STREAM` (parallel stream over rows), and `THREADS`
  (thread pool pulling pixels from a shared counter), with a configurable thread
  count via `setThreadsCount(...)`.

## Before / After

### Soft shadows (visual quality)

Area lights replace the hard, sharp-edged shadows of a point-sized source with
soft penumbrae. Same beach scene (`BeachScene2`), shadows only:

| Before — hard shadows | After — soft shadows |
| --- | --- |
| ![Beach with hard shadows](images/beach2_hard_shadows.png) | ![Beach with soft shadows](images/beach2_soft_shadows.png) |

**The call that makes the difference.** In `BeachScene2` flipping one flag turns
the point-sized sun into an area light:

```java
// unitTests/manual/BeachScene2.java
private static final boolean SOFT_SHADOWS = true;   // false = hard shadows (left image)
...
if (SOFT_SHADOWS)
    BeachScene.addLights(scene, SUN_RADIUS, SUN_RAYS);   // sun.setSize(radius).setNumOfRays(rays)
```

**Where it lives.** A light with a positive radius is sampled across its disk in
`SimpleRayTracer.transparency(...)` — instead of one shadow ray, it averages many,
and partially-occluded samples are what produce the gradual penumbra:

```java
// src/renderer/SimpleRayTracer.java — transparency(intersection, light, l, n)
List<Point> samples = new Blackboard()
    .setSize(radius)                       // light disk radius (0 = hard shadow)
    .setNumOfRays(light.getNumOfRays())    // shadow-ray samples across the disk
    .orient(lightPosition, l)
    .points();
Double3 ktrSum = Double3.ZERO;
for (Point sample : samples) {
    Vector toSample = sample.subtract(shiftedHead);
    ktrSum = ktrSum.add(shadowRayTransparency(shiftedHead, toSample, toSample.length()));
}
return ktrSum.divide(samples.size());      // average over all samples → soft edge
```

The disk sampling itself is the geometry-only `primitives/Blackboard.java`; the
area-light radius and sample count live on `lighting/PointLight.java`
(`setSize` / `setNumOfRays`).

### Multithreading (runtime)

Multithreading renders the **same** image — the win is wall-clock time, not
appearance. The thread pool spreads the pixels of a render across cores; on a
multi-core machine the parallel modes come in well under the single-threaded
baseline. `MultithreadingTests` renders the beach scene once per mode and prints
the timings and speedups.

**The call that makes the difference.** The render strategy is chosen on the
camera builder:

```java
// unitTests/manual/MultithreadingTests.java
Camera camera = Camera.getBuilder()
    // ...
    .setRenderMode(mode)   // SINGLE (baseline), STREAM, or THREADS
    .build();
```

## Current Test Coverage Areas

1. `primitives`: `Point`, `Vector`, `Ray`
2. `geometries.impl`: `Plane`, `Sphere`, `Triangle`, `Polygon`, `Tube`, `Cylinder`, `Geometries`
3. `renderer`:
   - `Camera` builder validation, ray construction, and camera-geometry integration
   - `ImageWriter` grid output
   - Emission and ambient rendering (`RenderStage6Tests`, `RenderTests`)
   - Lighting: `LightsTests`, `DirectionalLightTests`, `PointLightTests`, `SpotLightTests`, `MultiLightsTests`, `LightsFromFilesTests`
   - Shadows: `ShadowTests`, `SoftShadowTests`
   - Global effects: `TransparencyReflectionTests`
   - Acceleration/performance: `MultithreadingTests`
4. `scene`: `SceneLoaderTests` (XML/JSON loading)
5. `manual`: final-picture scenes (`BeachScene`, `BeachScene2`, `BeachBedScene`, `PalmTreeScene`)

## Notes

1. This repository currently uses IntelliJ module configuration instead of Maven/Gradle.
2. API documentation (Javadoc) is present across the main code and can be extended as features are added.
3. The camera supports builder-based setup for location, orientation, view-plane dimensions, view-plane distance, resolution, ray construction per pixel, render mode, and thread count.
4. Scene loading entry points are `SceneLoader.loadFromXml(...)`, `SceneLoader.loadFromJson(...)`, and `SceneLoader.load(...)`.
5. Bonus work completed across stages is tracked in `ProjectBonuses.md`.

## Maintenance

This README is intended to stay up to date with the codebase.
When new geometry types, lighting features, renderer capabilities, folders, or
test flows are added, update the relevant sections above.
