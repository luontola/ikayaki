/*
* ProjectEvent.java
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
 * ProjectEvent is used to notify others about the state change of a project.
 *
 * @author Esko Luontola
 */
public class ProjectEvent extends EventObject {

    /**
     * The project that sent this event.
     */
    private Project project;

    /**
     * The type of event this is.
     */
    private Type type;

    /**
     * Creates a new project event.
     *
     * @param project the project that sends this event.
     * @param type    the type of the event.
     * @throws NullPointerException if any of the arguments is null.
     */
    public ProjectEvent(Project project, Type type) {
        super(project);
        if (project == null || type == null) {
            throw new NullPointerException();
        }
        this.project = project;
        this.type = type;
    }

    /**
     * Returns the project that sent this event.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Returns the type of this event.
     */
    public Type getType() {
        return type;
    }

    /**
     * The type of a project event.
     */
    public enum Type {
        STATE_CHANGED, DATA_CHANGED, FILE_SAVED
    }
}