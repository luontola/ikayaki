/*
* GenericFileFilter.java
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

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * A FileFilter for a FileChooser. Can be used to show only some file types, determined by extension.
 *
 * @author Esko Luontola
 */
public class GenericFileFilter extends FileFilter implements java.io.FileFilter {

    /**
     * Extensions to be shown.
     */
    private String[] extensions;

    /**
     * File type desription for the extensions.
     */
    private String description;

    /**
     * Creates a new file filter for the specified file type. The file extensions can have "." prefixes or not.
     *
     * @param description a description for the file type, or null to have no description.
     * @param extensions  the file extensions that should be shown, or null to accept no extensions.
     */
    public GenericFileFilter(String description, String... extensions) {
        if (extensions == null) {
            extensions = new String[0];
        }
        if (description == null) {
            description = "";
        }
        for (int i = 0; i < extensions.length; i++) {
            while (extensions[i].startsWith(".")) {
                extensions[i] = extensions[i].substring(1);
            }
        }
        if (extensions.length > 0) {
            description += " (*." + extensions[0];
            for (int i = 1; i < extensions.length; i++) {
                description += ", *." + extensions[i];
            }
            description += ")";


        }

        this.extensions = extensions;
        this.description = description;
    }

    /**
     * Tests whether or not the specified abstract pathname should be included in a pathname list.
     *
     * @param pathname the abstract pathname to be tested.
     * @return true if and only if pathname should be included.
     */
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        }

        String extension = getExtension(pathname);
        if (extension != null) {
            for (int i = 0; i < this.extensions.length; i++) {
                if (extension.equals(this.extensions[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the extension of the given file.
     *
     * @param f the file which's extension is wanted
     * @return the characters after the last dot in the file name
     */
    private static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Returns the file type description.
     */
    public String getDescription() {
        return this.description;
    }
}