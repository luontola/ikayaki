/*
* CalibrationPanel.java
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

import ikayaki.Project;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * Holds predefined "Holder noise" and "Standard sample" projects for calibration; they are in a technically same table
 * as Project explorer files. Also has a "Calibrate" button, which executes selected calibration project, similarly to
 * clicking "Single step" in normal projects.
 *
 * @author
 */
public class CalibrationPanel extends ProjectComponent {
/*
Event A: On calibrateButton click - call project.doSingleStep(); show error message if
false is returned.
*/
/*
Event B: On calibrationProjectTable click - call Project.loadProject(File) with clicked
project file (calibrationProjectTable row); call MainViewPanel.changeProject(Project) with
returned Project unless null, on which case show error message and revert calibrationProjectTable
selection to old project, if any.
*/
/*
Event C: On ProjectEvent - highlight calibration project whose measuring started, or
unhighlight one whose measuring ended; enable calibrateButton if measuring has ended,
or disable if measuring has started.
*/

    /**
     * The component whose setProject() method will be called on opening a new project file.
     */
    private ProjectComponent parent;

    private JButton calibrateButton;

    /**
     * Table for the two calibration projects; has "filename", "last modified" and "time" (time since last modification)
     * columns.
     */
    private JTable calibrationProjectTable;

    /**
     * TableModel which holds the data for calibration projects. Unnamed inner class.
     */
    private TableModel calibrationProjectTableModel;

    /**
     * Creates a new calibration panel. Loads the contents of the program's calibration file directory.
     *
     * @param parent the parent component whose setProject() method will be called on opening a new project file.
     */
    public CalibrationPanel(ProjectComponent parent) {
        this.parent = parent;
        add(new JLabel("Calibration"));
        return; // TODO
    }

    /**
     * Call super.setProject(project), highlight selected calibration project, or unhighlight unselected calibration
     * project.
     */
    public void setProject(Project project) {
        return; // TODO
    }
}