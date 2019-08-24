package net.rogerfleenor;
//java
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileNotFoundException;
//sax
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;
//dom
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//json
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
//javax
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Author: Roger Fleenor
 * roger.fleenor@coxautoinc.com
 */

public class ManipulateXMLJSON {

    /**
     * Key = vin and value = trim are both of type string. Intended to store vin/trim pairs.
     */

    private static Map<String, String> vinTrimMap = new HashMap();

    /**
     * Iterates through text value until node type found,
     * Returns next non-text Node value or same value if is of type Node
     */

    public static Node filterWhiteSpace(Node sibling){
        while (null != sibling && sibling.getNodeType() != Node.ELEMENT_NODE) {
            sibling = sibling.getNextSibling();
        }
        return sibling;
    }

    /**
     * Reads from XML and finds matching VINs inserts new Node for override_trim
     * Returns nothing
     */

    public static void injectOverrideTrimToXML(String xmlfilenameinput, String xmlfilenameoutput){

        try {
            //File xmlFile = new File("uapi-error-en.xml");
            File xmlFile = new File(xmlfilenameinput);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance(); //Create the documentBuilderFactory
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder(); //Create the documentBuilder
            Document document = documentBuilder.parse(xmlFile); //Create the Document  by parsing the file
            Element documentElement = document.getDocumentElement(); //Get the root element of the xml Document;

            //System.out.println("documentElement: " + documentElement.toString());

            boolean isElement = false; //trigger this when element is found to end while

            NodeList allTypes =  documentElement.getElementsByTagName("vin"); //("Errors");

            for(int i = 0; i < allTypes.getLength(); i++) {

                Node type = allTypes.item(i);

                //System.out.println("Node "+ i + ": \n\t" + allTypes.item(i).getNodeName() + " " + allTypes.item(i).getTextContent()); //print vin node name + text content

                if(vinTrimMap.containsKey(allTypes.item(i).getTextContent())) { //check if VIN is in the HashMap

                    //System.out.println("MATCHING VIN FOUND!");

                } else {
                    continue; //skip to next item(i) if not desired vin
                }

                Node sibling = allTypes.item(i).getNextSibling(); //store next sibling node

                while(true){ //check for trim node

                    sibling = filterWhiteSpace(sibling); //filter whitespace else ignore

                    if (sibling.getNodeName().equalsIgnoreCase("trim")) {

                        //System.out.println("\t\t" + sibling.getNodeName() + " " + sibling.getTextContent()); //print next sibling Node Name + text content

                        //create element to append
                        Element override_trim = document.createElement("override_trim");

                        override_trim.appendChild(document.createTextNode(vinTrimMap.get(allTypes.item(i).getTextContent()))); //fetch from hash table the matching trim value for the key

                        //perform append here since desired Node found
                        sibling.getParentNode().insertBefore(override_trim, sibling.getNextSibling());

                        break; //exit while if trim Node found for sibling

                    } else {
                        sibling = sibling.getNextSibling();
                    }
                }
            }

            Transformer tFormer = TransformerFactory.newInstance().newTransformer();
            tFormer.setOutputProperty(OutputKeys.METHOD, "xml"); //Set output file to xml
            Source source = new DOMSource(document); //Write the document back to the file
            Result result = new StreamResult(xmlfilenameoutput);
            tFormer.transform(source, result);

            System.out.println("XML Manipulation SUCCESS");

            System.out.println("Check " + xmlfilenameoutput);

        } catch (TransformerException ex) {
            Logger.getLogger(ManipulateXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ManipulateXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ManipulateXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ManipulateXML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads JSON file, parses for vin and trim pair, adds to HashMap.
     * Returns nothing
     */

    public static void extractTrimByVinFromJSON(String filenameinput){

        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader(filenameinput)); //read from file
            JSONObject jsonObject = (JSONObject) obj; //create object to store JSON

            jsonObject = (JSONObject) jsonObject.get("vehicles");

            //System.out.println("key, value: "); //prepare to test output

            for (Object keyStr : jsonObject.keySet()) { //iterate through JSON Objects

                Object keyvalue = jsonObject.get(keyStr);

                //System.out.println(keyStr + " " + keyvalue); //test and output that objects are being found

                if (keyvalue instanceof JSONObject) { //nested object found https://www.baeldung.com/jsonobject-iteration

                    //System.out.println("NESTED OBJECT FOUND");

                    JSONObject keyvaluevintrim = (JSONObject) keyvalue; //get nested value

                    Object getvin =  keyvaluevintrim.get("vin");
                    Object gettrim = keyvaluevintrim.get("trim");

                    //System.out.println("getvin: " + getvin); //test output from getvin
                    //System.out.println("gettrim: " + gettrim); //test output from gettrim

                    vinTrimMap.put((String) getvin, (String) gettrim); //store vin and trim in HashMap

                   //System.out.println("getvin: " + getvin + "\n" + "gettrim: " + vinTrimMap.get((String) getvin));

                }
            }

            System.out.println("JSON Extraction SUCCESS");

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        // 1 - Read JSON, Store vin & trim in HashMap
        extractTrimByVinFromJSON("test2.json");

        // 2 - Read & Manipulate XML then Export
        injectOverrideTrimToXML("payload.xml", "outputpayload.xml");

    }
}