package renderer;

import static primitives.Util.isZero;

import java.util.MissingResourceException;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

/**
 * Represents a camera in 3D space that generates rays through a view plane.
 * <p>
 * The camera is configured through the nested {@link Builder}, which validates
 * the supplied data and computes the cached view-plane helper fields required
 * for ray construction.
 * </p>
 */
public class Camera implements Cloneable {
   /** Camera location. */
   private Point  _p0;
   /** Forward direction of the camera. */
   private Vector _vTo;
   /** Up direction of the camera. */
   private Vector _vUp;
   /** Right direction of the camera. */
   private Vector _vRight;

   /** View-plane width. */
   private double _width;
   /** View-plane height. */
   private double _height;
   /** Distance from the camera to the view plane. */
   private double _distance;

   /** Horizontal resolution (number of columns). */
   private int _nX = 1;
   /** Vertical resolution (number of rows). */
   private int _nY = 1;

   /** Center point of the view plane. */
   private Point  _vpCenter;
   /** Width of a single pixel. */
   private double _pixelWidth;
   /** Height of a single pixel. */
   private double _pixelHeight;

   /** Image writer used for coloring and saving pixels. */
   private ImageWriter _imageWriter;

   /** Ray tracer used for converting rays into colors. */
   private RayTracerBase _rayTracer;

   /** Private constructor; camera instances are created through the builder. */
   private Camera() {
   }

   /**
    * Returns a new builder for configuring a camera.
    *
    * @return fresh camera builder
    */
   public static Builder getBuilder() {
      return new Builder();
   }

   /**
    * Constructs a ray from the camera location through the given view-plane
    * pixel.
    *
    * @param xIndex column index of the target pixel
    * @param yIndex row index of the target pixel
    * @return ray from the camera through the pixel center
    */
   public Ray constructRay(int xIndex, int yIndex) {
      double xOffset = (xIndex - (_nX - 1) / 2d) * _pixelWidth;
      double yOffset = -(yIndex - (_nY - 1) / 2d) * _pixelHeight;

      Point pixelCenter = _vpCenter;

      if (!isZero(xOffset))
         pixelCenter = pixelCenter.add(_vRight.scale(xOffset));
      if (!isZero(yOffset))
         pixelCenter = pixelCenter.add(_vUp.scale(yOffset));

      return new Ray(_p0, pixelCenter.subtract(_p0));
   }

   /**
    * Renders the image by tracing a ray through every pixel.
    *
    * @return this camera for method chaining
    */
   public Camera renderImage() {
      if (_imageWriter == null)
         throw new IllegalStateException("Image writer is not initialized");
      if (_rayTracer == null)
         throw new IllegalStateException("Ray tracer is not initialized");

      for (int i = 0; i < _nY; i++) {
         for (int j = 0; j < _nX; j++) {
            castRay(_nX, _nY, j, i);
         }
      }
      return this;
   }

   /**
    * Traces one pixel ray and writes its resulting color.
    *
    * @param nX number of horizontal pixels
    * @param nY number of vertical pixels
    * @param j  column index
    * @param i  row index
    */
   private void castRay(int nX, int nY, int j, int i) {
      Ray ray = constructRay(j, i);
      _imageWriter.writePixel(j, i, _rayTracer.traceRay(ray));
   }

   /**
    * Draws a grid over the image at the given interval and color.
    *
    * @param interval grid line spacing in pixels
    * @param color    grid line color
    * @return this camera for method chaining
    */
   public Camera printGrid(int interval, Color color) {
      if (_imageWriter == null)
         throw new IllegalStateException("Image writer is not initialized");
      if (interval <= 0)
         throw new IllegalArgumentException("Grid interval must be positive");

      for (int i = 0; i < _nY; i++) {
         for (int j = 0; j < _nX; j++) {
            if (j % interval == 0 || i % interval == 0)
               _imageWriter.writePixel(j, i, color);
         }
      }
      return this;
   }

   /**
    * Writes the rendered image to disk.
    *
    * @param fileName output file name without extension
    */
   public void writeToImage(String fileName) {
      if (_imageWriter == null)
         throw new IllegalStateException("Image writer is not initialized");
      _imageWriter.writeToImage(fileName);
   }

   /**
    * Returns a shallow copy of this camera.
    *
    * @return cloned camera
    * @throws CloneNotSupportedException if cloning is unexpectedly unavailable
    */
   @Override
   protected Camera clone() throws CloneNotSupportedException {
      return (Camera) super.clone();
   }

   /**
    * Builder for assembling a validated {@link Camera} instance.
    */
   public static class Builder {
      /** Camera instance being configured. */
      private final Camera _camera = new Camera();
      /** Explicit forward direction supplied by the client, if any. */
      private Vector       _directionTo = null;
      /** Target point supplied by the client, if any. */
      private Point        _target      = null;
      /** General up hint used to derive the final orthogonal basis. */
      private Vector       _upHint      = Vector.AXIS_Y;

      /**
       * Sets the camera location.
       *
       * @param location camera location
       * @return this builder
       */
      public Builder setLocation(Point location) {
         _camera._p0 = location;
         return this;
      }

      /**
       * Sets the camera orientation from explicit forward and up vectors.
       *
       * @param to forward direction
       * @param up general up direction
       * @return this builder
       */
      public Builder setDirection(Vector to, Vector up) {
         _directionTo = to;
         _target = null;
         _upHint = up;
         return this;
      }

      /**
       * Sets the camera orientation from a target point and an up vector.
       *
       * @param target point the camera looks at
       * @param up     general up direction
       * @return this builder
       */
      public Builder setDirection(Point target, Vector up) {
         _directionTo = null;
         _target = target;
         _upHint = up;
         return this;
      }

      /**
       * Sets the camera orientation from a target point and the default Y-axis
       * up vector.
       *
       * @param target point the camera looks at
       * @return this builder
       */
      public Builder setDirection(Point target) {
         _directionTo = null;
         _target = target;
         _upHint = Vector.AXIS_Y;
         return this;
      }

      /**
       * Sets the view-plane size.
       *
       * @param width  view-plane width
       * @param height view-plane height
       * @return this builder
       */
      public Builder setVpSize(double width, double height) {
         _camera._width = width;
         _camera._height = height;
         return this;
      }

      /**
       * Sets the distance from the camera to the view plane.
       *
       * @param distance view-plane distance
       * @return this builder
       */
      public Builder setVpDistance(double distance) {
         _camera._distance = distance;
         return this;
      }

      /**
       * Sets the view-plane resolution.
       *
       * @param nX number of columns
       * @param nY number of rows
       * @return this builder
       */
      public Builder setResolution(int nX, int nY) {
         _camera._nX = nX;
         _camera._nY = nY;
         return this;
      }

      /**
       * Sets the camera ray tracer directly.
       *
       * @param rayTracer ray tracer instance
       * @return this builder
       */
      public Builder setRayTracer(RayTracerBase rayTracer) {
         _camera._rayTracer = rayTracer;
         return this;
      }

      /**
       * Sets the camera ray tracer by tracer type.
       *
       * @param scene         scene to be rendered
       * @param rayTracerType desired ray tracer type
       * @return this builder
       */
      public Builder setRayTracer(Scene scene, RayTracerType rayTracerType) {
         if (rayTracerType == RayTracerType.SIMPLE) {
            return setRayTracer(new SimpleRayTracer(scene));
         }
         throw new IllegalArgumentException("Unsupported ray tracer type: " + rayTracerType);
      }

      /**
       * Validates all camera data, computes the derived camera basis and
       * view-plane helper fields, and returns a copy of the configured camera.
       *
       * @return built camera instance
       */
      public Camera build() {
         checkResolution();
         checkLocationAndDirection();
         checkViewPlane();
         if (_camera._rayTracer == null)
            setRayTracer(new Scene("test"), RayTracerType.SIMPLE);
         try {
            return (Camera) _camera.clone();
         } catch (CloneNotSupportedException e) {
            return null;
         }
      }

      /**
       * Validates that the configured resolution values are positive.
       */
      private void checkResolution() {
         if (_camera._nX <= 0 || _camera._nY <= 0)
            throw new IllegalArgumentException("Resolution values must be positive");

         _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
      }

      /**
       * Validates and finalizes the camera location and orientation vectors.
       * <p>
       * When the forward direction was provided indirectly through a target
       * point, it is derived from the camera location to that target. The final
       * camera basis is normalized and orthogonalized before storing it in the
       * camera being built.
       * </p>
       */
      private void checkLocationAndDirection() {
         if (_camera._p0 == null)
            throw new MissingResourceException("Camera location is missing", Camera.class.getName(), "_p0");
         if (_upHint == null)
            throw new MissingResourceException("Camera up vector is missing", Camera.class.getName(), "_vUp");
         if (_directionTo == null && _target == null)
            throw new MissingResourceException("Camera direction is missing", Camera.class.getName(), "_vTo");

         Vector direction = _directionTo == null ? _target.subtract(_camera._p0) : _directionTo;
         _camera._vTo = direction.normalize();
         _camera._vRight = _camera._vTo.crossProduct(_upHint).normalize();
         _camera._vUp = _camera._vRight.crossProduct(_camera._vTo).normalize();
      }

      /**
       * Validates the view-plane dimensions and distance, then computes the
       * cached helper fields used during ray construction.
       */
      private void checkViewPlane() {
         if (_camera._width <= 0 || _camera._height <= 0 || _camera._distance <= 0)
            throw new IllegalArgumentException("View plane values must be positive");

         _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));
         _camera._pixelWidth = _camera._width / _camera._nX;
         _camera._pixelHeight = _camera._height / _camera._nY;
      }
   }
}
