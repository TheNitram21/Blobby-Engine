package de.arnomann.martin.blobby.core;

import org.joml.*;
import org.joml.Math;

/**
 * A camera used for rendering.
 */
public class Camera {

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f viewProjectionMatrix;

    private FrustumIntersection frustumIntersection;

    private float left, right, bottom, top;

    private Vector2f position = new Vector2f();
    private float rotation = 0f;
    private float zoom = 1f;

    /**
     * Creates a new camera.
     * @param left the left frustum plane.
     * @param right the right frustum plane.
     * @param bottom the bottom frustum plane.
     * @param top the top frustum plane.
     */
    public Camera(float left, float right, float bottom, float top) {
        try {
            this.left = left;
            this.right = right;
            this.bottom = bottom;
            this.top = top;

            projectionMatrix = new Matrix4f().ortho(left, right, bottom, top, -1f, 1f);
            viewMatrix = new Matrix4f();

            viewProjectionMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);

            frustumIntersection = new FrustumIntersection(viewProjectionMatrix);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the position of the camera.
     * @return the camera's position.
     */
    public Vector2f getPosition() {
        return position;
    }

    /**
     * Sets the position of the camera.
     * @param position the new position.
     */
    public void setPosition(Vector2f position) {
        this.position = position;
        recalculateViewMatrix();
    }

    /**
     * Returns the rotation of the camera.
     * @return the camera's rotation
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation of the camera.
     * @param rotation the new rotation.
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
        recalculateViewMatrix();
    }

    /**
     * Returns the zoom of the camera.
     * @return the camera's zoom
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Sets the zoom of the camera. A zoom of {@code 2f} means that the player can see twice as much.
     * @param zoom the new zoom.
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
        recalculateViewMatrix();
    }

    /**
     * Returns the camera's projection matrix.
     * @return the projection matrix.
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * Returns the camera's view matrix.
     * @return the view matrix.
     */
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    /**
     * Returns the camera's view projection matrix (projection matrix * view matrix).
     * @return the view projection matrix.
     */
    public Matrix4f getViewProjectionMatrix() {
        return viewProjectionMatrix;
    }

    /**
     * Returns the camera's left frustum plane.
     * @return the left frustum plane.
     */
    public float getLeft() {
        return left;
    }

    /**
     * Returns the camera's right frustum plane.
     * @return the right frustum plane.
     */
    public float getRight() {
        return right;
    }

    /**
     * Returns the camera's width.
     * @return the width.
     */
    public float getWidth() {
        return right - left;
    }

    /**
     * Returns the camera's bottom frustum plane.
     * @return the bottom frustum plane.
     */
    public float getBottom() {
        return bottom;
    }

    /**
     * Returns the camera's top frustum plane.
     * @return the top frustum plane.
     */
    public float getTop() {
        return top;
    }

    /**
     * Returns the camera's height.
     * @return the height.
     */
    public float getHeight() {
        return top - bottom;
    }

    public boolean couldRender(float x1, float y1, float x2, float y2) {
        return frustumIntersection.testAab(Math.min(x1, x2), Math.min(y1, y2), 0, Math.max(x1, x2), Math.max(y1, y2),
                0);
    }

    private void recalculateViewMatrix() {
        Matrix4f transform = new Matrix4f().translate(new Vector3f(position, 0f))
                .mul(new Matrix4f().rotate(Math.toRadians(rotation), new Vector3f(0, 0, 1)))
                .mul(new Matrix4f().scale(zoom, zoom, 1f));

        viewMatrix = transform.invert();
        viewProjectionMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);

        frustumIntersection.set(viewProjectionMatrix);
    }

}
