/*
* MeasurementValue.java
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

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Algorithms for calculating values from the measurements. A MeasurementValue object will be passed to the getValue()
 * method of a project to retrieve the desired value.
 *
 * @author Esko Luontola
 */
public abstract class MeasurementValue <T> {

    /**
     * Calculates the average of all X components.
     */
    public static final MeasurementValue<Double> X = new MeasurementValue<Double>("X", "mT", "Average of measured X components") {
        public Double getValue(MeasurementStep step) {
            double sum = 0.0;
            int count = step.getResults();
            for (int i = 0; i < count; i++) {
                sum += step.getResult(i).getX();
            }
            if (count > 0) {
                return new Double(sum / count);
            } else {
                return null;
            }
        }
    };

    /**
     * Calculates the average of all Y components.
     */
    public static final MeasurementValue<Double> Y = new MeasurementValue<Double>("Y", "mT", "Average of measured Y components") {
        public Double getValue(MeasurementStep step) {
            double sum = 0.0;
            int count = step.getResults();
            for (int i = 0; i < count; i++) {
                sum += step.getResult(i).getY();
            }
            if (count > 0) {
                return new Double(sum / count);
            } else {
                return null;
            }
        }
    };

    /**
     * Calculates the average of all Z components.
     */
    public static final MeasurementValue<Double> Z = new MeasurementValue<Double>("Z", "mT", "Average of measured Z components") {
        public Double getValue(MeasurementStep step) {
            double sum = 0.0;
            int count = step.getResults();
            for (int i = 0; i < count; i++) {
                sum += step.getResult(i).getZ();
            }
            if (count > 0) {
                return new Double(sum / count);
            } else {
                return null;
            }
        }
    };

    /**
     * Calculates the declination from the component averages.
     */
    public static final MeasurementValue<Double> DECLINATION = new MeasurementValue<Double>("D", "\u00b0", "Geographic declination") {
        public Double getValue(MeasurementStep step) {

            return null; // TODO
        }
    };

    /**
     * Calculates the inclination from the component averages.
     */
    public static final MeasurementValue<Double> INCLINATION = new MeasurementValue<Double>("I", "\u00b0", "Geographic inclination") {
        public Double getValue(MeasurementStep step) {
            return null; // TODO
        }
    };

    /**
     * Calculates the length of the vector from the component averages.
     */
    public static final MeasurementValue<Double> MOMENT = new MeasurementValue<Double>("M", "mT", "Length of the magnetization vector") {
        public Double getValue(MeasurementStep step) {
            Double x = X.getValue(step);
            Double y = Y.getValue(step);
            Double z = Z.getValue(step);
            if (x == null || y == null || z == null) {
                return null;
            } else {
                return sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2));
            }
        }
    };

    /**
     * Calculates the remanence from the component averages and the sample�s volume.
     */
    public static final MeasurementValue<Double> REMANENCE = new MeasurementValue<Double>("J", "?", "Magnitude of the magnetization (M/V=J)") {
        public Double getValue(MeasurementStep step) {
            Project project = step.getProject();
            double volume = step.getVolume();
            if (volume < 0.0 && project != null) {
                volume = project.getVolume();
            }
            if (volume <= 0.0) {
                return null;
            }
            Double moment = MOMENT.getValue(step);
            if (moment == null) {
                return null;
            } else {
                return moment / volume;
            }
        }
    };

    /**
     * Calculates the remanence relative to the first measurement�s remanence.
     */
    public static final MeasurementValue<Double> RELATIVE_REMANENCE = new MeasurementValue<Double>("J/J0", "", "Relative magnitude of the magnetization") {
        public Double getValue(MeasurementStep step) {
            return null; // TODO
        }
    };

    /**
     * Calculates the Theta 63 value from the measurement result set.
     */
    public static final MeasurementValue<Double> THETA63 = new MeasurementValue<Double>("caption", "unit", "description") {
        public Double getValue(MeasurementStep step) {
            return null; // TODO
        }
    };

    /**
     * A short name for the value.
     */
    private String caption;
    /**
     * The unit of the value.
     */
    private String unit;
    /**
     * A long description of the value.
     */
    private String description;

    /**
     * Creates a new measurement value.
     *
     * @param caption     a short name for the value.
     * @param unit        the unit of the value.
     * @param description a long description of the value.
     * @throws NullPointerException if any of the arguments is null.
     */
    public MeasurementValue(String caption, String unit, String description) {
        if (caption == null || unit == null || description == null) {
            throw new NullPointerException();
        }
        this.caption = caption;
        this.unit = unit;
        this.description = description;
    }

    /**
     * Calculates a specific value from a measurement step.
     *
     * @param step the step from which the value will be calculated.
     * @return the calculated value, or null if it was not possible to calculate it.
     * @throws NullPointerException if step is null.
     */
    abstract T getValue(MeasurementStep step);

    /**
     * Returns a short name for the value.
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Returns the unit of the value.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns a long description of the value.
     */
    public String getDescription() {
        return description;
    }
}

enum Test {

    TEST1(){
        public void test() {
        }
    },

    TEST2(){
        public void test() {
        }
    };

    public abstract void test();
}