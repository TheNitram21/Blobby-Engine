package de.arnomann.martin.blobby.core;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f viewProjectionMatrix;

    private float left, right, bottom, top;

    private Vector2f position = new Vector2f();
    private float rotation = 0f;

    public Camera(float left, float right, float bottom, float top) {
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;

        projectionMatrix = new Matrix4f().ortho(left, right, bottom, top, -1f, 1f);
        viewMatrix = new Matrix4f();

        viewProjectionMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
        recalculateViewMatrix();
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        recalculateViewMatrix();
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getViewProjectionMatrix() {
        return viewProjectionMatrix;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public float getWidth() {
        return right - left;
    }

    public float getBottom() {
        return bottom;
    }

    public float getTop() {
        return top;
    }

    public float getHeight() {
        return top - bottom;
    }

    private void recalculateViewMatrix() {
        Matrix4f transform = new Matrix4f().translate(new Vector3f(position, 0f))
                .mul(new Matrix4f().rotate(Math.toRadians(rotation), new Vector3f(0, 0, 1)));

        viewMatrix = transform.invert();
        viewProjectionMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);
    }

}
