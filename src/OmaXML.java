/* @ Aki Korpua
/* date: 6.3.2005
/*
/* Vaikee selkoinen XML-apuluokan raakile
/* sis‰lt‰‰ metodit:
/* public static Document buildDom() : luodaan DOM data-rakenne
/* public static Document loadDom(String filename) : ladataan XML-tiedosto DOM rakenteeseen
/* public static void outputDom(Document document) : tulostetaan ruudulle DOM XML muodossa
/* public static void saveXML(Document document,String filename) : Talletetaan DOM XML-tiedostoksi
/* public static void readDocument(Document document) : k‰yd‰‰n v‰h‰n testiksi l‰pi DOM-rakennetta
/* public static String nodeToString(Node domNode) : apuluokka Noden tulostamiseen
/*
/* Document tiedoston k‰pistelyyn tarvittava data: http://java.sun.com/xml/jaxp/dist/1.1/docs/api/org/w3c/dom/Document.html
/* 
*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
        saveXML(doc1, "output.xml");
        Document doc2 = loadDom("output.xml");
        outputDom(doc2);
        readDocument(doc2);
        //System.out.println("-------------------------------------------------------------------");
        //outputDom(doc2);
    }

    /**
     * Luodaan ihQ oma DOM puu. T‰lle voisi sitten argumenttina antaa Project-luokan esimerkiksi
     */
    public static Document buildDom() {
        Document document;
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            //mit‰h‰n kaikkee voikaan tehd‰ t‰ll‰, kivaa... t‰ss‰ perusjuttu

            Element root =
                    (Element) document.createElement("Pohja");
            Element second =
                    (Element) document.createElement("Toka");
            document.appendChild(root);
            root.appendChild(second);
            root.appendChild(document.createTextNode("Paljon Superia"));
            root.appendChild(document.createTextNode("tekstia "));
            root.appendChild(document.createTextNode("^^"));
            second.appendChild(document.createTextNode("Liss‰‰ teksti‰ palajo/nJeijei"));

            document.getDocumentElement().normalize();

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
            // Error generated during parsing)
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
            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(uloste);
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
        } catch (FileNotFoundException fe) {
            System.out.println("file not found");
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