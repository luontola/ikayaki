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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

/**
 * A set of X, Y and Z values measured by the magnetometer. The raw XYZ values will be rotated in 3D space by using a
 * transformation matrix. The project will set and update the transformation whenever its parameters are changed.
 * <p/>
 * All units are mA/m.
 *
 * @author Esko Luontola
 */
public class MeasurementResult {

    // TODO: getters for returning copies of the vectors

    /**
     * The type of this result.
     */
    private Type type;

    /**
     * The rotation that the sample holder was in when this result was measured. The value is in range 0..360 degrees.
     */
    private int rotation;

    /**
     * The unmodified measurements recieved from the squid. Will not change after it has been once set.
     */
    private Vector3d rawVector = new Vector3d();

    /**
     * The measurements in sample coordinates. Has the rotation, noise and holder fixes applied to itself.
     */
    private Vector3d sampleVector = new Vector3d();

    /**
     * The measurements in geographic coordinates. Equals the sample coordinates with the transformation matrix
     * applied.
     */
    private Vector3d geographicVector = new Vector3d();

    /**
     * Creates a new measurement result. All units are mA/m.
     * <p/>
     * The sample and geographic coordinates are NOT set when a MeasurementResult is created.
     *
     * @param type     the type (background or rotation) of this result.
     * @param rotation the rotation of the sample holder in degrees (0..360).
     * @param x        the measured X coordinate value.
     * @param y        the measured Y coordinate value.
     * @param z        the measured Z coordinate value.
     * @throws NullPointerException if type is null.
     */
    public MeasurementResult(Type type, int rotation, double x, double y, double z) {
        if (type == null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.rotation = rotation % 360;
        rawVector.set(x, y, z);
        applyFixes(null);
        setTransform(null);
    }

    /**
     * Creates a measurement result from the specified element.
     * <p/>
     * The sample and geographic coordinates are NOT set when a MeasurementResult is created.
     *
     * @param element the element from which this result will be created.
     * @throws NullPointerException     if element is null.
     * @throws IllegalArgumentException if the element was not in the right format.
     */
    public MeasurementResult(Element element) {
        if (element == null) {
            throw new NullPointerException();
        }

        // verify tag name
        if (!element.getTagName().equals("result")) {
            throw new IllegalArgumentException("Invalid tag name: " + element.getTagName());
        }

        // get type
        String type = element.getAttribute("type");
        try {
            this.type = Type.valueOf(type);
        } catch (IllegalArgumentException e) {
            // TODO: import old versions
            //throw new IllegalArgumentException("Invalid type: " + type);
            if (type.equals("BG")) {
                this.type = Type.NOISE;
            } else {
                this.type = Type.SAMPLE;
            }
        }

        // get rotation
        try {
            this.rotation = Integer.parseInt(element.getAttribute("rotation")) % 360;
        } catch (NumberFormatException e) {
            // TODO: import old versions
            //throw new IllegalArgumentException("Invalid rotation: " + e.getMessage());
            if (type.equals("BG")) {
                this.rotation = 0;
            } else {
                this.rotation = Integer.parseInt(type);
            }
        }

        // get x, y, z
        try {
            rawVector.set(Double.parseDouble(element.getAttribute("x")),
                    Double.parseDouble(element.getAttribute("y")),
                    Double.parseDouble(element.getAttribute("z")));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid x, y, z: " + e.getMessage());
        }

        // initialize sampleVector and geographicVector
        applyFixes(null);
        setTransform(null);
    }

    /**
     * Exports this result to a DOM element.
     *
     * @param document the document that will contain this element.
     */
    public Element getElement(Document document) {
        Element element = document.createElement("result");

        element.setAttribute("type", type.name());
        element.setAttribute("rotation", Integer.toString(rotation));
        element.setAttribute("x", Double.toString(rawVector.x));
        element.setAttribute("y", Double.toString(rawVector.y));
        element.setAttribute("z", Double.toString(rawVector.z));

        return element;
    }

    /**
     * Applies the holder, noise and rotation fixes and saves the results as the sample vector. Resets the geographic
     * vector to a copy of the sample vector. This method must be called before setTransform().
     *
     * @param step the measurement step that includes the holder and noise calibration values. If null, the holder and
     *             noise fixes are not applied.
     */
    protected void applyFixes(MeasurementStep step) {
        sampleVector.set(rawVector);

        // apply holder and noise fixes
        if (step != null) {
            Vector3d holder = step.getHolder();
            Vector3d noise = step.getNoise();
            sampleVector.x = sampleVector.x - holder.x - noise.x;
            sampleVector.y = sampleVector.y - holder.y - noise.y;
            sampleVector.z = sampleVector.z - holder.z - noise.z;
        }

        // apply rotation fix
        Matrix3d rotate = new Matrix3d();
        rotate.rotZ(Math.toRadians(rotation));
        rotate.transform(sampleVector);

        // reset geographic vector
        setTransform(null);
    }

    /**
     * Applies a transformation matrix to the sample vector and saves the results as the geographic vector. This method
     * must be called after applyFixes().
     *
     * @param transform the matrix to be applied. If null, will assume identity matrix.
     */
    protected void setTransform(Matrix3d transform) {
        if (transform != null) {
            transform.transform(sampleVector, geographicVector);
        } else {
            geographicVector.set(sampleVector);
        }
    }

    /**
     * Returns the type of this result.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the rotation of this result. The value is in range 0..360 degrees.
     */
    public int getRotation() {
        return rotation;
    }

    /**
     * Returns the noise fixed, rotated and transformed X coordinate of this result. The value is in geographic
     * coordinates.
     */
    public double getGeographicX() {
        // TODO: throw exception if not a SAMPLE measurement
        return geographicVector.x;
    }

    /**
     * Returns the noise fixed, rotated and transformed Y coordinate of this result. The value is in geographic
     * coordinates.
     */
    public double getGeographicY() {
        // TODO: throw exception if not a SAMPLE measurement
        return geographicVector.y;
    }

    /**
     * Returns the noise fixed, rotated and transformed Z coordinate of this result. The value is in geographic
     * coordinates.
     */
    public double getGeographicZ() {
        // TODO: throw exception if not a SAMPLE measurement
        return geographicVector.z;
    }

    /**
     * Returns the length of the geographic vector. Should be always the same as the sample vector.
     */
    public double getGeographicLength() {
        return geographicVector.length();
    }

    /**
     * Returns the noise fixed and rotated X coordinate of this result. The value is in sample coordinates.
     */
    public double getSampleX() {
        return sampleVector.x;
    }

    /**
     * Returns the noise fixed and rotated Y coordinate of this result. The value is in sample coordinates.
     */
    public double getSampleY() {
        return sampleVector.y;
    }

    /**
     * Returns the noise fixed and rotated Z coordinate of this result. The value is in sample coordinates.
     */
    public double getSampleZ() {
        return sampleVector.z;
    }

    /**
     * Returns the length of the sample vector.
     */
    public double getSampleLength() {
        return sampleVector.length();
    }

    /**
     * Returns the unmodified X coordinate of this result. The value is in magnetometer coordinates.
     */
    public double getRawX() {
        return rawVector.x;
    }

    /**
     * Returns the unmodified Y coordinate of this result. The value is in magnetometer coordinates.
     */
    public double getRawY() {
        return rawVector.y;
    }

    /**
     * Returns the unmodified Z coordinate of this result. The value is in magnetometer coordinates.
     */
    public double getRawZ() {
        return rawVector.z;
    }

    /**
     * Returns the length of the raw vector.
     */
    public double getRawLength() {
        return rawVector.length();
    }

//    @Override public String toString() {
//        return "[result type=" + type + " value=(" + geographicVector.x + ", " + geographicVector.y + ", " + geographicVector.z + ")]";
//    }


    public enum Type {
        SAMPLE, HOLDER, NOISE
    }

//    /**
//     * The orientation of the sample when it was measured.
//     *
//     * @author Esko Luontola
//     */
//    public enum Type {
//        BG("BG"), DEG0("0"), DEG90("90"), DEG180("180"), DEG270("270");
//
//        private String name;
//
//        private Type(String name) {
//            this.name = name;
//        }
//
//        /**
//         * Returns "BG", "0", "90", "180" or "270".
//         */
//        public String getName() {
//            return name;
//        }
//
//        /**
//         * Returns the same as getName().
//         */
//        @Override public String toString() {
//            return getName();
//        }
//
//        /**
//         * Rotates the specified vector from the orientation of this object to that of DEG0. Rotating a BG or DEG0 will
//         * just copy the values directly.
//         *
//         * @param t old values that need to be rotated.
//         * @return a new object with the rotated values.
//         */
//        public Vector3d rotate(Vector3d t) {
//            return rotate(t, null);
//        }
//
//        /**
//         * Rotates the specified vector from the orientation of this object to that of DEG0. Rotating a BG or DEG0 will
//         * just copy the values directly.
//         *
//         * @param t      old values that need to be rotated.
//         * @param result where the new values will be saved.
//         * @return the same as the result parameter, or a new object if it was null.
//         */
//        public Vector3d rotate(Vector3d t, Vector3d result) {
//            if (result == null) {
//                result = new Vector3d();
//            }
//            switch (this) {
//            case BG:
//            case DEG0:
//                result.set(t.x, t.y, t.z);
//                break;
//            case DEG90:
//                result.set(-t.y, t.x, t.z);
//                break;
//            case DEG180:
//                result.set(-t.x, -t.y, t.z);
//                break;
//            case DEG270:
//                result.set(t.y, -t.x, t.z);
//                break;
//            default:
//                assert false;
//                break;
//            }
//            return result;
//        }
//    }
}