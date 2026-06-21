# Soft Shadows — Design (RDD)

Status: **implemented.** See `primitives/Blackboard.java`, `setSize`/`setNumOfRays`
on `lighting/PointLight.java` (inherited by `SpotLight`), and the soft-shadow
branch in `renderer/SimpleRayTracer.transparency(...)`.

**On/off:** soft shadows are per-light and **off by default**. A light uses the
single-ray hard-shadow path unless *both* `setSize(radius > 0)` and
`setNumOfRays(n > 1)` are set on it — so every existing scene renders unchanged.

Resolved open questions: naming is `Blackboard` with `setSize`/`setNumOfRays`;
the ray count is given as a flat "number of rays" (mapped internally to a
per-axis density of `ceil(sqrt(n))`); DOF/AA reuse the same class later.

This covers Part 1 (the reusable sampling infrastructure) and how Part 2
(area-light soft shadows + the sunset effect) builds on it. The infrastructure
is deliberately **not** tied to soft shadows — the same sampler will later serve
depth-of-field and anti-aliasing.

---

## 1. The sampling entity — `Blackboard`

### Package & responsibility (RDD)
- **Package: `primitives`.**
- **Responsibility (one sentence):** *given a 2D target area placed in 3D space,
  produce a set of sample points that cover that area as uniformly as the chosen
  pattern allows.*
- Rationale: it is a pure **geometric service** — 2D offsets + a local 3D basis.
  It depends only on `Point`/`Vector`, and is needed by both `lighting` (area
  lights) and `renderer` (camera DOF/AA). Placing it in `primitives` keeps it a
  shared low-level primitive and avoids a `lighting ↔ renderer` dependency or an
  arbitrary "wherever I first used it" home. (Rejected: `renderer` — would force
  `lighting` to depend up into `renderer`.)

### What it stores
- `Point center` — where the area sits in space.
- `Vector vUp, vRight` — the **local orthonormal 2D→3D basis** (see §6).
- `double size` — circle radius / square half-edge.
- `Shape shape`, `Pattern pattern`, `int density` (samples per axis → ~`density²`).
- A **cached list of 2D offsets** (see §3) and a dirty flag.

### Public surface (sketch — names open to your spec)
```
package primitives;
public class Blackboard {
    public enum Shape   { CIRCLE, SQUARE }
    public enum Pattern { GRID, JITTERED, STOCHASTIC }
    // configuration (returns this for chaining): size, density, shape, pattern
    // placement: orient(Point center, Vector normal)   // builds vUp/vRight from normal
    // query:    List<Point> points()                    // 2D offsets mapped to 3D
}
```

## 2. Target-area shape — **circle** (default), square supported
- **Circle**, justified: light sources (sun disk, bulb) and camera apertures are
  round, so a disk gives an **isotropic penumbra** with no axis-aligned banding.
- Implementation: generate offsets on a square grid and **keep only those inside
  the inscribed circle** (rejection), giving uniform disk coverage without the
  pole-clustering of naïve polar sampling.
- `SQUARE` is kept in the enum because **anti-aliasing** samples a square pixel —
  same class, different shape, no special-casing at the call sites.

## 3. Point model — **store 2D offsets, map to 3D on demand**
- Store each sample as a 2D offset `(u, v)` in `[-size, +size]`.
- Map when queried: `p3 = center + vRight·u + vUp·v`.
- Rationale: the **pattern** (distribution within the area) is independent of the
  area's position/orientation. For a point light, the basis is recomputed **per
  shaded point** (the area is orthogonal to `l`, which changes per surface point);
  caching 3D points would be useless, but the **2D offset pattern can be cached
  once** and re-mapped cheaply for every query. This decouples *pattern* from
  *placement* — exactly the reuse the assignment asks for.
- A tiny value type holds the offset: nested `record Sample(double u, double v)`
  (the "object value corresponding to a 2D offset" the brief mentions).

## 4. Pattern — selectable, default **jittered grid**
- `GRID`: deterministic cell centers — reproducible, good for unit tests.
- `JITTERED` (**default**): grid cells, each sampled at a random point *within its
  cell* — uniform coverage **and** breaks the regular pattern that causes banding.
- `STOCHASTIC`: pure uniform-random — simplest, but noisier for the same count.
- All three aim for uniform coverage; the enum lets us add patterns later without
  touching callers.

## 5. When is the list (re)generated? — on parameter change
- The **2D offset list is cached** and rebuilt only when size/density/shape/pattern
  change (dirty flag).
- `GRID` is fully deterministic → cache and reuse everywhere.
- `JITTERED`/`STOCHASTIC`: we cache **one** randomized set per parameter change and
  reuse it. Reusing a fixed jittered set across queries is the standard
  performance choice and the per-cell jitter already removes banding; per-query
  regeneration (for extra decorrelation) can be added behind a flag if a result
  ever shows correlation artifacts.

## 6. Local coordinate system
- Given the area normal `d` (a point light's `l`, a spotlight's `dir`):
  1. pick a world axis least parallel to `d`,
  2. `vRight = d × axis` (normalized),
  3. `vUp = d × vRight` (normalized).
- Reused verbatim by the camera, where `vRight`/`vUp` are the existing camera axes.

## 7. The central ray — not auto-injected
- We do **not** force the area's center point into the sample set.
- For a jittered/stochastic disk the center is just one possible sample; injecting
  it separately would bias the average toward the center. We let the pattern decide.
- This is also what makes the **sunset effect** correct (§9): the central ray to
  the sun's center may be *blocked by the horizon*, yet off-center samples still
  contribute — and the blocked ones still count as zero in the average.

---

## 8. Part 2 — how soft shadows use this
- Add `setSize(double)` (radius) to **`PointLight`** and **`SpotLight`**.
  `size = 0` → single ray → current hard shadow (no behavior change for existing
  scenes). **`DirectionalLight` is not supported** (no position to give an area).
- In `SimpleRayTracer.transparency(...)`: when the light has `size > 0`, build a
  `Blackboard` oriented **orthogonal to `l`** (point) / **to `dir`** (spot) at the
  light position, get its sample points, run the **existing blocker-accumulation**
  toward each sample, and **average** the per-ray `Double3` results. `size = 0`
  keeps the single-ray path. Diffuse/specular/refraction math is untouched.

## 9. Part 2 — the sunset effect (the showcase)
- With the sun modeled as an **area light low on the horizon**, the central ray
  to its center is occluded by the horizon, but samples on the upper part of the
  disk clear it and contribute → the sun **dims and softens gradually** instead of
  snapping off, and shadows gain wide penumbrae.
- Requires only §8 plus the rule in §7 (occluded samples included as zero in the
  average). The sunset recolor already in `BeachScene` positions the sun for this.

---

## Open questions for you (match to your spec before I code)
1. **Naming**: `Blackboard` ok, or does your grader want `TargetArea` / specific
   method names (`setSize`, `setNumOfRays`, etc.)?
2. **Density parameter**: expose as "rays per axis" (`density²` total) or as a flat
   "number of rays"?
3. Is **anti-aliasing / DOF** expected to reuse this in the same submission (affects
   how general I make the camera-facing API now vs. later)?
