/*
* MeasurementResult.java
*
* Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
*
* This file is part of Ikayaki.
*
* Ikayaki is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* Ikayaki is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Ikayaki; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/

package ikayaki;

import org.w3c.dom.Element;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

/**
 * A set of X, Y and Z values measured by the magnetometer. The raw XYZ values will be rotated in 3D space by using a
 * transformation matrix. The project will set and update the transformation whenever its parameters are changed.
 *
 * @author Esko Luontola
 */
public class MeasurementResult {

    /**
     * The type of this result. Can be either a background noise measurement, or a sample in one of four rotations (0,
     * 90, 180 or 270 degrees).
     */
    private Type type;

    /**
     * The unmodified measurements recieved from the squid.
     */
    private Vector3d rawVector = new Vector3d();

    /**
     * The measurements with the rotation and transformation matrix applied.
     */
    private Vector3d vector = new Vector3d();

    /**
     * Creates a new measurement result.
     *
     * @param type the type (background or rotation) of this result.
     * @param x    the measured X coordinate value.
     * @param y    the measured Y coordinate value.
     * @param z    the measured Z coordinate value.
     * @throws NullPointerException if type is null.
     */
    public MeasurementResult(Type type, double x, double y, double z) {
        if (type == null) {
            throw new NullPointerException();
        }
        this.type = type;
        rawVector.set(x, y, z);
        setTransform(null);     // initialize this.vector with an identity matrix
    }

    /**
     * Creates a measurement result from the specified element. This will not apply the transformation matrix, so the
     * user must apply it manually.
     *
     * @param element the element from which this result will be created.
     * @throws NullPointerException     if element is null.
     * @throws IllegalArgumentException if the element was not in the right format.
     */
    public MeasurementResult(Element element) {
        if (element == null) {
            throw new NullPointerException();
        }
        setTransform(null);     // initialize this.vector with an identity matrix

        return; // TODO;
    }

    /**
     * Exports this result to a DOM element.
     */
    public Element getElement() {
        return null; // TODO
    }

    /**
     * Applies a transformation matrix to this result.
     *
     * @param transform the matrix to be applied. If null, will assume identity matrix.
     */
    void setTransform(Matrix3d transform) {
        type.rotate(rawVector, vector);     // copy rawVector to vector and rotate the values
        if (transform != null) {
            transform.transform(vector);    // apply transformation matrix
        }
    }

    /**
     * Returns the type of this result (background or rotation).
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the rotated and transformed X coordinate of this result.
     */
    public double getX() {
        return vector.x;
    }

    /**
     * Returns the rotated and transformed Y coordinate of this result.
     */
    public double getY() {
        return vector.y;
    }

    /**
     * Returns the rotated and transformed Z coordinate of this result.
     */
    public double getZ() {
        return vector.z;
    }

    /**
     * Returns the unmodified X coordinate of this result as recieved from the Squid.
     */
    public double getRawX() {
        return rawVector.x;
    }

    /**
     * Returns the unmodified Y coordinate of this result as recieved from the Squid.
     */
    public double getRawY() {
        return rawVector.y;
    }

    /**
     * Returns the unmodified Z coordinate of this result as recieved from the Squid.
     */
    public double getRawZ() {
        return rawVector.z;
    }

    /**
     * The orientation of the sample when it was measured.
     *
     * @author Esko Luontola
     */
    public enum Type {
        BG("BG"), DEG0("0"), DEG90("90"), DEG180("180"), DEG270("270");

        private String name;

        private Type(String name) {
            this.name = name;
        }

        /**
         * Returns "BG", "0", "90", "180" or "270".
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the same as getName().
         */
        @Override public String toString() {
            return getName();
        }

        /**
         * Rotates the specified vector from the orientation of this object to that of DEG0. Rotating a BG or DEG0 will
         * just copy the values directly.
         *
         * @param t old values that need to be rotated.
         * @return a new object with the rotated values.
         */
        public Vector3d rotate(Vector3d t) {
            return rotate(t, null);
        }

        /**
         * Rotates the specified vector from the orientation of this object to that of DEG0. Rotating a BG or DEG0 will
         * just copy the values directly.
         *
         * @param t      old values that need to be rotated.
         * @param result where the new values will be saved.
         * @return the same as the result parameter, or a new object if it was null.
         */
        public Vector3d rotate(Vector3d t, Vector3d result) {
            if (result == null) {
                result = new Vector3d();
            }
            switch (this) {
            case BG:
            case DEG0:
                result.set(t.x, t.y, t.z);
                break;
            case DEG90:
                result.set(-t.y, t.x, t.z);
                break;
            case DEG180:
                result.set(-t.x, -t.y, t.z);
                break;
            case DEG270:
                result.set(t.y, -t.x, t.z);
                break;
            default:
                assert false;
                break;
            }
            return result;
        }
    }
}