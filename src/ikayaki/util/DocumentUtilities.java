/*
 * DocumentUtilities.java
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

package ikayaki.util;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Tools for reading and writing DOM documents.
 *
 * @author Esko Luontola
 */
public class DocumentUtilities {

    /**
     * Emits an XML document representing the specified DOM document.
     *
     * @param file     the file on which to emit the XML document.
     * @param document the document to be emitted.
     * @return true if the operation was successful, otherwise false.
     * @throws NullPointerException if any of the arguments is null.
     */
    public static boolean storeToXML(File file, Document document) {
        if (file == null || document == null) {
            throw new NullPointerException();
        }
        try {
            return storeToXML(new FileOutputStream(file), document);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Emits an XML document representing the specified DOM document.
     *
     * @param out      the output stream on which to emit the XML document.
     * @param document the document to be emitted.
     * @return true if the operation was successful, otherwise false.
     * @throws NullPointerException if any of the arguments is null.
     */
    public static boolean storeToXML(OutputStream out, Document document) {
        if (out == null || document == null) {
            throw new NullPointerException();
        }
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute("indent-number", new Integer(2));

            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new OutputStreamWriter(out, "utf-8"));
            t.transform(source, result);
            return true;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Returns a DOM document represented by the XML document on the specified file.
     *
     * @param file the file from which to read the XML document.
     * @return the loaded document, or null if there was an error in loading it.
     * @throws NullPointerException if any of the arguments is null.
     */
    public static Document loadFromXML(File file) {
        if (file == null) {
            throw new NullPointerException();
        }
        try {
            return loadFromXML(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a DOM document represented by the XML document on the specified input stream.
     *
     * @param in the input stream from which to read the XML document.
     * @return the loaded document, or null if there was an error in loading it.
     * @throws NullPointerException if any of the arguments is null.
     */
    public static Document loadFromXML(InputStream in) {
        if (in == null) {
            throw new NullPointerException();
        }
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(in);
            return document;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
