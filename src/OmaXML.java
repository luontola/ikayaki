/* @ Aki Korpua
 * date: 6.3.2005
 *
 * Vaikee selkoinen XML-apuluokan raakile
 * sis‰lt‰‰ metodit:
 * public static Document buildDom() : luodaan DOM data-rakenne (esimerkki project-xml)
 * public static Document loadDom(String filename) : ladataan XML-tiedosto DOM rakenteeseen
 * public static void outputDom(Document document) : tulostetaan ruudulle DOM XML muodossa
 * public static void saveXML(Document document,String filename) : Talletetaan DOM XML-tiedostoksi
 * public static void readDocument(Document document) : k‰yd‰‰n v‰h‰n testiksi l‰pi DOM-rakennetta
 * public static String nodeToString(Node domNode) : apuluokka Noden tulostamiseen
 *
 * Document tiedoston k‰pistelyyn tarvittava data: http://java.sun.com/xml/jaxp/dist/1.1/docs/api/org/w3c/dom/Document.html
 *
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

class OmaXML {
    // An array of names for DOM node-types
    // (Array indexes = nodeType() values.)
    static final String[] typeName = {
        "none",
        "Element",
        "Attr",
        "Text",
        "CDATA",
        "EntityRef",
        "Entity",
        "ProcInstr",
        "Comment",
        "Document",
        "DocType",
        "DocFragment",
        "Notation",
    };


    public static void main(String[] args) {
        Document doc1 = buildDom();
        saveXML(doc1, "example.xml");
        Document doc2 = loadDom("example.xml");
        outputDom(doc2);
        //readDocument(doc2);
    }

    /**
     * Luodaan ihQ oma DOM puu t‰lle voisi sitten argumenttina antaa Project-luokan esimerkiksi
     */
    public static Document buildDom() {
        Document document;
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            // Esimerkki mahdollisesta projektitiedostosta
            Element root =
                    (Element) document.createElement("project");
            document.appendChild(root);
            root.setAttribute("version", "1.0");
            root.setAttribute("type", "af");

            Element properties = (Element) document.createElement("properties");
            root.appendChild(properties);
            Element operator = (Element) document.createElement("operator");
            properties.appendChild(operator);
            operator.setAttribute("value", "Erkki");
            Element date = (Element) document.createElement("date");
            properties.appendChild(date);
            date.setAttribute("value", "14.6.1997 13:02");
            Element ismanual = (Element) document.createElement("ismanual");
            properties.appendChild(ismanual);
            ismanual.setAttribute("value", "false");
            Element rocktype = (Element) document.createElement("rocktype");
            properties.appendChild(rocktype);
            rocktype.setAttribute("value", "Basalt");
            Element sampletype = (Element) document.createElement("sampletype");
            properties.appendChild(sampletype);
            sampletype.setAttribute("value", "hand");
            Element site = (Element) document.createElement("site");
            properties.appendChild(site);
            site.setAttribute("value", "Antarctis, 1134, Utpostane");
            Element comment = (Element) document.createElement("comment");
            properties.appendChild(comment);
            comment.setAttribute("value", "Dike 10m tos, fine grained, altered");
            Element latitude = (Element) document.createElement("latitude");
            properties.appendChild(latitude);
            latitude.setAttribute("value", "344");
            Element longitude = (Element) document.createElement("longitude");
            properties.appendChild(longitude);
            longitude.setAttribute("value", "-74");
            Element strike = (Element) document.createElement("strike");
            properties.appendChild(strike);
            strike.setAttribute("value", "280");
            Element dip = (Element) document.createElement("dip");
            properties.appendChild(dip);
            dip.setAttribute("value", "14");
            Element mass = (Element) document.createElement("mass");
            properties.appendChild(mass);
            mass.setAttribute("value", "10");
            Element volume = (Element) document.createElement("volume");
            properties.appendChild(volume);
            volume.setAttribute("value", "");

            Element sequence = (Element) document.createElement("sequence");
            root.appendChild(sequence);
            sequence.setAttribute("name", "Basalt");
            for (int i = 0; i < 10; i++) {
                Element step = (Element) document.createElement("step");
                sequence.appendChild(step);
                step.setAttribute("timestamp", "2005-02-08 14:43:00");
                step.setAttribute("stepvalue", "" + i);
                step.setAttribute("mass", "");
                step.setAttribute("volume", "");
                for (int j = 0; j < 6; j++) {
                    Element measurement = (Element) document.createElement("measurement");
                    step.appendChild(measurement);
                    measurement.setAttribute("rotation", "" + j);
                    measurement.setAttribute("x", i + "" + j);
                    measurement.setAttribute("y", i + "" + j);
                    measurement.setAttribute("z", i + "" + j);
                }
            }

            return document;
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();

        }
        return null;
    }

    /**
     * Ladataan kivaan DOM-kikkareeseen XML filu
     */
    public static Document loadDom(String filename) {
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(filename));
            return document;

        } catch (SAXException sxe) {
            // Error generated during parsing
            Exception x = sxe;
            if (sxe.getException() != null) {
                x = sxe.getException();
            }
            x.printStackTrace();

        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();

        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        }

        return null;
    }

    /**
     * ulostetaan DOM XML muodossa
     */
    public static void outputDom(Document document) {
        try {
            //OutputStream uloste;
            // Use a Transformer for output
            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

        } catch (TransformerConfigurationException tce) {
            // Error generated by the parser
            System.out.println("\n** Transformer Factory error");
            System.out.println("   " + tce.getMessage());

            // Use the contained exception, if any
            Throwable x = tce;
            if (tce.getException() != null) {
                x = tce.getException();
            }
            x.printStackTrace();

        } catch (TransformerException te) {
            // Error generated by the parser
            System.out.println("\n** Transformation error");
            System.out.println("   " + te.getMessage());

            // Use the contained exception, if any
            Throwable x = te;
            if (te.getException() != null) {
                x = te.getException();
            }
            x.printStackTrace();
        }
    }

    /**
     * talletetaan filuun
     */
    public static void saveXML(Document document, String filename) {
        try {
            FileOutputStream uloste = new FileOutputStream(filename);

            // Muunnetaan DOM XML:ksi apuluokalla
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute("indent-number", new Integer(2));

            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new OutputStreamWriter(uloste, "utf-8"));
            t.transform(source, result);

        } catch (TransformerConfigurationException tce) {
            // Error generated by the parser
            System.out.println("\n** Transformer Factory error");
            System.out.println("   " + tce.getMessage());

            // Use the contained exception, if any
            Throwable x = tce;
            if (tce.getException() != null) {
                x = tce.getException();
            }
            x.printStackTrace();

        } catch (TransformerException te) {
            // Error generated by the parser
            System.out.println("\n** Transformation error");
            System.out.println("   " + te.getMessage());

            // Use the contained exception, if any
            Throwable x = te;
            if (te.getException() != null) {
                x = te.getException();
            }
            x.printStackTrace();
        } catch (FileNotFoundException fe) {
            System.out.println("file not found");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            assert false;
        }
    }

    /**
     * luetaan DOM rakennetta
     */
    public static void readDocument(Document document) {
        // haetaan Ensimm‰inen "pohja"-elementti.
        NodeList list = document.getElementsByTagName("Pohja");
        Node node = list.item(0);
        System.out.println(nodeToString(node));

        // Otetaan "pohja"-elementin eka lapsonen k‰sittelyyn.
        Node subNode = node.getFirstChild();
        System.out.println(nodeToString(subNode));

        //k‰yd‰‰n kaikki l‰pi
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            System.out.println(nodeToString(children.item(i)));
        }
    }

    /**
     * ulostaa Noden kivana Stringin‰
     */
    public static String nodeToString(Node domNode) {
        String s = typeName[domNode.getNodeType()];
        String nodeName = domNode.getNodeName();
        if (!nodeName.startsWith("#")) {
            s += ": " + nodeName;
        }
        if (domNode.getNodeValue() != null) {
            if (s.startsWith("ProcInstr")) {
                s += ", ";
            } else {
                s += ": ";
            }
            
            // Trim the value to get rid of NL's at the front
            String t = domNode.getNodeValue().trim();
            int x = t.indexOf("\n");
            if (x >= 0) t = t.substring(0, x);
            s += t;
        }
        return s;
    }
}