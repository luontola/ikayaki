/*
* MeasurementEvent.java
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

import java.util.EventObject;

/**
 * MeasurementEvent is used to notify listeners about the stages of an ongoing measurement.
 *
 * @author Esko Luontola
 */
public class MeasurementEvent extends EventObject {

    /**
     * The project whose measurement sent this event.
     */
    private Project project;

    /**
     * The measurement that sent this event.
     */
    private MeasurementStep step;

    /**
     * The type of event this is.
     */
    private Type type;

    /**
     * Creates a new measurement event.
     *
     * @param project the project whose measurement sent this event.
     * @param step    the measurement that sent this event.
     * @param type    the type of event this is.
     * @throws NullPointerException if any of the arguments is null.
     */
    public MeasurementEvent(Project project, MeasurementStep step, Type type) {
        super(project);
        if (project == null || step == null || type == null) {
            throw new NullPointerException();
        }
        this.project = project;
        this.step = step;
        this.type = type;
    }

    /**
     * Returns the project whose measurement sent this event.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Returns the measurement that sent this event.
     */
    public MeasurementStep getStep() {
        return step;
    }

    /**
     * Returns the type of event this is.
     */
    public Type getType() {
        return type;
    }

    /**
     * The type of a measurement event.
     */
    public enum Type {
        STEP_START,
        STEP_END,
        STEP_ABORTED,
        HANDLER_MOVE,
        HANDLER_ROTATE,
        HANDLER_STOP,
        DEMAGNETIZE_START,
        DEMAGNETIZE_END,
        VALUE_MEASURED
    }
}