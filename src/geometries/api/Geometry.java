package geometries.api;

import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;

import java.io.Serializable;

/**
 * Base type for geometric objects.
 */
public abstract class Geometry extends Intersectable {

    /** Emission color contributed by this geometry. */
    private Color emission = Color.BLACK;
    /** Material coefficients of this geometry. */
    private Material material = new Material();

    /**
     * Returns the geometry emission color.
     *
     * @return emission color
     */
    public Color getEmission() {
        return emission;
    }

    /**
     * Sets the geometry emission color.
     *
     * @param emission emission color
     * @return this geometry for method chaining
     */
    public Geometry setEmission(Color emission) {
        if (emission == null) {
            throw new IllegalArgumentException("Emission must not be null");
        }
        this.emission = emission;
        return this;
    }

    /**
     * Returns the geometry material.
     *
     * @return material data
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Sets the geometry material.
     *
     * @param material material data
     * @return this geometry for method chaining
     */
    public Geometry setMaterial(Material material) {
        if (material == null) {
            throw new IllegalArgumentException("Material must not be null");
        }
        this.material = material;
        return this;
    }


    /**
     * Returns the normal vector to the geometry at the given point.
     *
     * @param point a point on the surface of the geometry
     * @return the normalized normal vector at that point
     */
    public abstract Vector getNormal(Point point);
}
