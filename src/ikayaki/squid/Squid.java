/*
* Squid.java
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

package ikayaki.squid;

import ikayaki.Project;

/**
 * Offers an interface for controlling the SQUID system. Reads settings from the Settings class. Creates instances of
 * the degausser, handler and magnetometer classes and offers handles for them.
 *
 * @author
 */
public class Squid {

    /**
     * Instance of the Squid interface.
     */
    private static Squid instance;

    /**
     * The project that is currently using the Squid, or null if no project is using it.
     */
    private Project owner;

    /**
     * Instance of the degausser interface.
     */
    private Degausser degausser;

    /**
     * Instance of the handler interface.
     */
    private Handler handler;

    /**
     * Instance of the magnetometer interface.
     */
    private Magnetometer magnetometer;

    /**
     * Returns a reference to the Squid. If it has not yet been created, will create one.
     */
    public static synchronized Squid instance() {
        return null; // TODO
    }

    /**
     * Initializes the Squid interface. Creates instances of Degausser, Handler and Magnetometer.
     */
    private Squid() {
        return; // TODO
    }

    /**
     * Returns an interface for controlling the degausser.
     *
     * @return Handler for Degausser if available
     */
    public Degausser getDegausser() {
        return null; // TODO
    }

    /**
     * Returns an interface for controlling the handler.
     *
     * @return Handler for Handler if available
     */
    public Handler getHandler() {
        return null; // TODO
    }

    /**
     * Returns an interface for controlling the magnetometer.
     *
     * @return Handler for Magnetometer if available
     */
    public Magnetometer getMagnetometer() {
        return null; // TODO
    }

    /**
     * Checks which settings have changed and updates all the device interfaces. This method should be called when
     * changes are made to the device parameters.
     * <p/>
     * This method starts a worker thread that will update the settings. If the current project has a measurement
     * running, the thread will keep on retrying until the measurement is finished. Multiple calls to this method within
     * a short period of time will update the settings only once.
     */
    public synchronized void updateSettings() {
        return; // TODO
    }

    /**
     * Checks whether all devices are working correctly.
     *
     * @return true if everything is correct, otherwise false.
     */
    public synchronized boolean isOK() {
        return false; // TODO
    }

    /**
     * Sets the owner of the Squid. Only one project may have access to the Squid at a time. This method may be called
     * only from the Project class.
     *
     * @param owner the project that will have exclusive access to the Squid.
     * @return true if successful, false if the existing owner had a running measurement.
     */
    public synchronized boolean setOwner(Project owner) {
        return false; // TODO
    }

    /**
     * Returns project that is currently using the Squid.
     *
     * @return the project, or null if none is using the Squid.
     */
    public synchronized Project getOwner() {
        return null; // TODO
    }
}