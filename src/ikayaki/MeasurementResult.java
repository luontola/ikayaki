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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static ikayaki.MeasurementResult.Type.*;

/**
 * A set of X, Y and Z values measured by the magnetometer. The raw XYZ values will be rotated in 3D space by using a
 * transformation matrix. The project will set and update the transformation whenever its parameters are changed.
 * <p/>
 * All units are mA/m.
 *
 * @author Esko Luontola
 */
public class MeasurementResult {

    // TODO: change the Type values to SAMPLE, NOISE, HOLDER

    // TODO: add property "int rotation" and getters for it. allow rotations also others than 0, 90, 180, 270.

    // TODO: getters for returning copies of the vectors

    /**
     * The type of this result. Can be either a background noise measurement, or a sample in one of four rotations (0,
     * 90, 180 or 270 degrees).
     */
    private Type type;

    /**
     * The unmodified measurements recieved from the squid. Will not change after it has been once set.
     */
    private Vector3d rawVector = new Vector3d();

    /**
     * The measurements with the rotation applied. Will not change after it has been once set.
     */
    private Vector3d sampleVector = new Vector3d();

    /**
     * The measurements with the rotation and transformation matrix applied.
     */
    private Vector3d geographicVector = new Vector3d();

    /**
     * Creates a new measurement result. All units are mA/m.
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
        this.type.rotate(rawVector, sampleVector);
        setTransform(null);
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

        // verify tag name
        if (!element.getTagName().equals("result")) {
            throw new IllegalArgumentException("Invalid tag name: " + element.getTagName());
        }

        // get type
        String type = element.getAttribute("type");
        if (type.equals(BG.toString())) {
            this.type = BG;
        } else if (type.equals(DEG0.toString())) {
            this.type = DEG0;
        } else if (type.equals(DEG90.toString())) {
            this.type = DEG90;
        } else if (type.equals(DEG180.toString())) {
            this.type = DEG180;
        } else if (type.equals(DEG270.toString())) {
            this.type = DEG270;
        } else {
            throw new IllegalArgumentException("Invalid type: " + type);
        }

        // get x, y, z
        try {
            rawVector.set(Double.parseDouble(element.getAttribute("x")),
                    Double.parseDouble(element.getAttribute("y")),
                    Double.parseDouble(element.getAttribute("z")));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + e.getMessage());
        }

        // initialize sampleVector and geographicVector
        this.type.rotate(rawVector, sampleVector);
        setTransform(null);
    }

    /**
     * Exports this result to a DOM element.
     *
     * @param document the document that will contain this element.
     */
    public Element getElement(Document document) {
        Element element = document.createElement("result");

        element.setAttribute("type", type.toString());
        element.setAttribute("x", Double.toString(rawVector.x));
        element.setAttribute("y", Double.toString(rawVector.y));
        element.setAttribute("z", Double.toString(rawVector.z));

        return element;
    }

    /**
     * Applies a transformation matrix to this result.
     *
     * @param transform the matrix to be applied. If null, will assume identity matrix.
     */
    protected void setTransform(Matrix3d transform) {
        if (transform != null) {
            transform.transform(sampleVector, geographicVector);
        }
    }

    /**
     * Returns the type of this result (background or rotation).
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the rotated and transformed X coordinate of this result. The value is in geographic coordinates.
     */
    public double getGeographicX() {
        // TODO: throw exception if not a SAMPLE measurement
        return geographicVector.x;
    }

    /**
     * Returns the rotated and transformed Y coordinate of this result. The value is in geographic coordinates.
     */
    public double getGeographicY() {
        // TODO: throw exception if not a SAMPLE measurement
        return geographicVector.y;
    }

    /**
     * Returns the rotated and transformed Z coordinate of this result. The value is in geographic coordinates.
     */
    public double getGeographicZ() {
        // TODO: throw exception if not a SAMPLE measurement
        return geographicVector.z;
    }

    /**
     * Returns the rotated X coordinate of this result. The value is in sample coordinates.
     */
    public double getSampleX() {
        return sampleVector.x;
    }

    /**
     * Returns the rotated Y coordinate of this result. The value is in sample coordinates.
     */
    public double getSampleY() {
        return sampleVector.y;
    }

    /**
     * Returns the rotated Z coordinate of this result. The value is in sample coordinates.
     */
    public double getSampleZ() {
        return sampleVector.z;
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

    @Override public String toString() {
        return "[result type=" + type + " value=(" + geographicVector.x + ", " + geographicVector.y + ", " + geographicVector.z + ")]";
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

    public static void main(String[] args) {
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        MeasurementResult r = new MeasurementResult(DEG90, 1, 2.15019801981090189109019091098, 3);
        System.out.println(r);
        System.out.println(new MeasurementResult(r.getElement(document)));

        for (int j = 0; j < 10; j++) {
            long time = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                r.getElement(document);
            }
            time = System.currentTimeMillis() - time;
            System.out.println("getElement(): " + time / 10000.0 + " ms/call");
        }
    }
}