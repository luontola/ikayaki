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

import static java.lang.Math.atan2;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.PI;
import static java.lang.Math.atan;
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
     * Calculates the average of all X components in geographic coordinates.
     */
    public static final MeasurementValue<Double> GEOGRAPHIC_X =
            new MeasurementValue<Double>("X'", "mA/m", "Mean X (geographic coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = step.getResults();
                    for (int i = 0; i < count; i++) {
                        sum += step.getResult(i).getGeographicX();
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the average of all Y components in geographic coordinates.
     */
    public static final MeasurementValue<Double> GEOGRAPHIC_Y =
            new MeasurementValue<Double>("Y'", "mA/m", "Mean Y (geographic coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = step.getResults();
                    for (int i = 0; i < count; i++) {
                        sum += step.getResult(i).getGeographicY();
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the average of all Z components in geographic coordinates.
     */
    public static final MeasurementValue<Double> GEOGRAPHIC_Z =
            new MeasurementValue<Double>("Z'", "mA/m", "Mean Z (geographic coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = step.getResults();
                    for (int i = 0; i < count; i++) {
                        sum += step.getResult(i).getGeographicZ();
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the average of all X components in sample coordinates.
     */
    public static final MeasurementValue<Double> SAMPLE_X =
            new MeasurementValue<Double>("X", "mA/m", "Mean X (sample coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = step.getResults();
                    for (int i = 0; i < count; i++) {
                        sum += step.getResult(i).getSampleX();
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the average of all Y components in sample coordinates.
     */
    public static final MeasurementValue<Double> SAMPLE_Y =
            new MeasurementValue<Double>("Y", "mA/m", "Mean Y (sample coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = step.getResults();
                    for (int i = 0; i < count; i++) {
                        sum += step.getResult(i).getSampleY();
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the average of all Z components in sample coordinates.
     */
    public static final MeasurementValue<Double> SAMPLE_Z =
            new MeasurementValue<Double>("Z", "mA/m", "Mean Z (sample coordinates)") {
                public Double getValue(MeasurementStep step) {
                    double sum = 0.0;
                    int count = step.getResults();
                    for (int i = 0; i < count; i++) {
                        sum += step.getResult(i).getSampleZ();
                    }
                    if (count > 0) {
                        return new Double(sum / count);
                    } else {
                        return null;
                    }
                }
            };

    /**
     * Calculates the declination from the component averages in geographic coordinates.
     */
    public static final MeasurementValue<Double> DECLINATION =
            new MeasurementValue<Double>("D", "\u00b0", "Geographic declination") {
                public Double getValue(MeasurementStep step) {
                    Double x = GEOGRAPHIC_X.getValue(step);
                    Double y = GEOGRAPHIC_Y.getValue(step);
                    if (x == null || y == null) {
                        return null;
                    } else {
                        double d = atan2(y, x);
                        if (d < 0.0) {
                            d += PI * 2;
                        }
                        return Math.toDegrees(d);
                    }
                }
            };

    /**
     * Calculates the inclination from the component averages in geographic coordinates.
     */
    public static final MeasurementValue<Double> INCLINATION =
            new MeasurementValue<Double>("I", "\u00b0", "Geographic inclination") {
                public Double getValue(MeasurementStep step) {
                    Double x = GEOGRAPHIC_X.getValue(step);
                    Double y = GEOGRAPHIC_Y.getValue(step);
                    Double z = GEOGRAPHIC_Z.getValue(step);
                    if (x == null || y == null || z == null) {
                        return null;
                    } else {
                        if (x == 0.0) {
                            x = 0.000000000001;
                        }
                        if (y == 0.0) {
                            y = 0.000000000001;
                        }
                        if (z == 0) System.out.println(sqrt(pow(x, 2) + pow(y, 2)));
                        double d = atan(z / sqrt(pow(x, 2) + pow(y, 2)));
                        return Math.toDegrees(d);
                    }
                }
            };

    /**
     * Calculates the length of the vector from the component averages.
     */
    public static final MeasurementValue<Double> MOMENT =
            new MeasurementValue<Double>("M", "Am^2", "Magnetic moment") {
                public Double getValue(MeasurementStep step) {
                    Double x = SAMPLE_X.getValue(step);
                    Double y = SAMPLE_Y.getValue(step);
                    Double z = SAMPLE_Z.getValue(step);
                    if (x == null || y == null || z == null) {
                        return null;
                    } else {
                        return sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2));
                    }
                }
            };

    /**
     * Calculates the magnetic intensity (or remanence) from the moment and the sample's volume or mass
     * (depending on the selected normalization).
     */
    public static final MeasurementValue<Double> MAGNETIZATION =
            new MeasurementValue<Double>("J", "Am^2/kg,mA/m", "Magnetic intensity (J=M/volume or J=M/mass)") {
                public Double getValue(MeasurementStep step) {
                    Project project = step.getProject();
                    if (project == null) {
                        return null;
                    }

                    double normalizer;
                    if (project.getNormalization() == Project.Normalization.VOLUME) {
                        normalizer = step.getVolume();
                        if (normalizer < 0.0) {
                            normalizer = project.getVolume();
                        }
                    } else if (project.getNormalization() == Project.Normalization.MASS) {
                        normalizer = step.getMass();
                        if (normalizer < 0.0) {
                            normalizer = project.getMass();
                        }
                    } else {
                        assert false;
                        return null;
                    }
                    if (normalizer <= 0.0) {
                        return null;
                    }

                    Double moment = MOMENT.getValue(step);
                    if (moment == null) {
                        return null;
                    } else {
                        return moment / normalizer;
                    }
                }
            };

    /**
     * Calculates the magnetic intensity (or remanence) relative to the first measurement's magnetic intensity.
     */
    public static final MeasurementValue<Double> RELATIVE_MAGNETIZATION =
            new MeasurementValue<Double>("J/J0", "", "Relative magnetic intensity") {
                public Double getValue(MeasurementStep step) {
                    Project project = step.getProject();
                    if (project == null) {
                        return null;
                    }
                    Double j = MAGNETIZATION.getValue(step);
                    Double j0 = MAGNETIZATION.getValue(project.getStep(0));     // there is at least one step in the project
                    if (j == null || j0 == null) {
                        return null;
                    } else {
                        return j.doubleValue() / j0.doubleValue();
                    }
                }
            };

    /**
     * Calculates the angular standard deviation (Theta 63) from the measurement result set.
     */
    public static final MeasurementValue<Double> THETA63 =
            new MeasurementValue<Double>("\u03b863", "\u00b0", "Angular standard deviation") {
                public Double getValue(MeasurementStep step) {
                    if (step.getResults() == 0) {
                        return null;
                    }
//                  double sumL = 0.0;
//                  double sumM = 0.0;
//                  double sumN = 0.0;
                    double sumL2 = 0.0;
                    double sumM2 = 0.0;
                    double sumN2 = 0.0;

                    for (int i = 0; i < step.getResults(); i++) {
                        MeasurementResult r = step.getResult(i);
                        double declination = atan(r.getSampleX() / r.getSampleY());
                        double inclination = atan(r.getSampleZ() /
                                sqrt(pow(r.getSampleX(), 2) + pow(r.getSampleY(), 2)));
                        double l = cos(declination) * cos(inclination);
                        double m = sin(declination) * cos(inclination);
                        double n = sin(inclination);
//                      sumL += l;
//                      sumM += m;
//                      sumN += n;
                        sumL2 += l * l;
                        sumM2 += m * m;
                        sumN2 += n * n;
                    }

                    double R = sqrt(sumL2 + sumM2 + sumN2);
                    double k = (step.getResults() - 1) / (step.getResults() - R);
                    return 81.0 / sqrt(k);
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