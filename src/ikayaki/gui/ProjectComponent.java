/*
* ProjectComponent.java
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

package ikayaki.gui;

import ikayaki.*;

import javax.swing.*;

/**
 * Common superclass for components which use a Project and listen to MeasurementEvents and ProjectEvents.
 *
 * @author Esko Luontola
 */
public class ProjectComponent extends JPanel implements ProjectListener, MeasurementListener {

    /**
     * The active project.
     */
    private Project project;

    /**
     * Initializes this ProjectComponent with no project.
     */
    public ProjectComponent() {
        super();
        project = null;
    }

    /**
     * Returns the active project, or null if no project is active.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Sets the project for this ProjectComponent. Unregisters MeasurementListener and ProjectListener from the old
     * project, and registers them to the new project.
     *
     * @param project new active project, or null to make no project active.
     */
    public void setProject(Project project) {
        if (this.project != null) {
            this.project.removeProjectListener(this);
            this.project.removeMeasurementListener(this);
        }
        if (project != null) {
            project.addProjectListener(this);
            project.addMeasurementListener(this);
        }
        this.project = project;
    }

    /**
     * Does nothing; subclasses override this if they want to listen ProjectEvents.
     *
     * @param event ProjectEvent received.
     */
    public void projectUpdated(ProjectEvent event) {
        // DOES NOTHING
    }

    /**
     * Does nothing; subclasses override this if they want to listen MeasurementEvents.
     *
     * @param event MeasurementEvent received.
     */
    public void measurementUpdated(MeasurementEvent event) {
        // DOES NOTHING
    }
}