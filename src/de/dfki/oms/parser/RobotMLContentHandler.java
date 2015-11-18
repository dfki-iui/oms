package de.dfki.oms.parser;

import de.dfki.omm.impl.OMMBlockImpl;
import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.interfaces.OMM;
import de.dfki.omm.interfaces.OMMBlock;
import de.dfki.omm.types.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * SAXHandler to parse RobotML documents.
 *
 * Created by xekl01 on 25.09.2015.
 */
public class RobotMLContentHandler extends DefaultHandler {

    private String omsURL;

    /**
     * Constructor.
     *
     * @param omsURL Host and port of the OMS on which to create memories from RobotML.
     */
    public RobotMLContentHandler(String omsURL) {
        super();
        this.omsURL = omsURL;
    }

    private ArrayList<OMM> parsedOMMs;
    private OMM currentOMM;
    private OMMBlock currentBlock;
    private ParserMode currentMode = ParserMode.General;
    private StringBuilder currentText = new StringBuilder();

    private  OMMEntity creator;

    private int attributeCounter = 1;
    private int operationCounter = 1;
    private int dataFlowPortCounter = 1;
    private int servicePortCounter = 1;

    private String cleartextName = "Robot ML XML";
    private String username = "robotML";
    private String password = "robotML";

    private final String robotMLElementComponent = "Component"; // RobotML component node (will be turned into an OMM)
    private final String robotMLElementAttribute = "Attribute"; // RobotML attribute node (will be turned into an attribute block)
    private final String robotMLElementOperation = "Operation"; // RobotML operation node (will be turned into an operation block)
    private final String robotMLElementDataFlowPort = "DataFlowPort"; // RobotML operation node (will be turned into an data flow port block)
    private final String robotMLElementServicePort = "ServicePort"; // RobotML operation node (will be turned into an service port block)

    @Override
    public void startDocument() throws SAXException {

//        System.out.println("---- start document");

        // initialize before parsing
        parsedOMMs = new ArrayList<OMM>();
        String date = ISO8601.getISO8601StringWithGMT();
        creator = new OMMEntity("api", "RobotML Parser", date);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

//        System.out.println("---- startElement: "+uri+", "+localName+", "+qName+", "+attributes+" | in mode: "+currentMode);

        // collect subnodes of "Operation" or "ServicePort"
        if (currentOMM != null && (currentMode.equals(ParserMode.Operation) || currentMode.equals(ParserMode.ServicePort)) ) {
//            System.out.println("---- ---- ... collecting contents of Operation or ServicePort ...");
            currentText.append("<");
            currentText.append(qName);
            for (int i = 0; i < attributes.getLength(); i++) {
                currentText.append(" ");
                currentText.append(attributes.getLocalName(i));
                currentText.append("=");
                currentText.append(attributes.getValue(i));
            }
            currentText.append(">");
        }

        // collect all other nodes
        else {

            OMMMultiLangText title;

            switch (qName) {

                // component node -> create OMM
                case robotMLElementComponent:
//                    System.out.println("---- ---- ... parsing Component '"+attributes.getValue("Name")+"' ...");
                    GenericTypedValue primaryID = new GenericTypedValue("url", omsURL+"/rest/" + attributes.getValue("Name"));
                    currentOMM = OMMFactory.createEmptyOMM(primaryID);
                    attributeCounter = 1;
                    operationCounter = 1;
                    dataFlowPortCounter = 1;
                    servicePortCounter = 1;
                    break;

                // attribute node -> create atribute block
                case robotMLElementAttribute:
                    if (currentOMM != null) {
//                        System.out.println("---- ---- ... parsing Attribute '"+attributes.getValue("Name")+"'...");
                        title = new OMMMultiLangText();
                        title.put(Locale.ENGLISH, "Attribute:"+attributes.getValue("Name"));
                        try {
                            currentBlock = OMMBlockImpl.create("Attribute"+ attributeCounter++, currentOMM.getHeader().getPrimaryID(),
                                    URI.create("urn:RobotML:attribute"), new URL("http://purl.org/dc/dcmitype/Text"), title, null, null,
                                    creator, new OMMFormat("text/plain", null, null), null, new GenericTypedValue("text/plain", ""),
                                    null, null, null);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        currentMode = ParserMode.Attribute;
                        currentText = currentText.replace(0, currentText.length(), "");
                    }
                    break;

                // operation node -> create operation block
                case robotMLElementOperation:
                    if (currentOMM != null) {
//                        System.out.println("---- ---- ... parsing Operation '"+attributes.getValue("Name")+"'...");
                        title = new OMMMultiLangText();
                        title.put(Locale.ENGLISH, "Operation:"+attributes.getValue("Name"));
                        currentBlock = OMMBlockImpl.create("Operation"+ operationCounter++, currentOMM.getHeader().getPrimaryID(),
                                URI.create("urn:RobotML:operation"), null, title, null, null, creator, new OMMFormat("application/xml", null, null),
                                null, new GenericTypedValue("application/xml", ""), null, null, null);
                        currentMode = ParserMode.Operation;
                        currentText = currentText.replace(0, currentText.length(), "");
                    }
                    break;

                // dataflowport node -> create dataflowport block
                case robotMLElementDataFlowPort:
                    if (currentOMM != null) {
//                        System.out.println("---- ---- ... parsing DataFlowPort '"+attributes.getValue("Name")+"'...");
                        title = new OMMMultiLangText();
                        title.put(Locale.ENGLISH, "DataFlowPort:"+attributes.getValue("Name"));
                        try {
                            currentBlock = OMMBlockImpl.create("DataFlowPort"+ dataFlowPortCounter++, currentOMM.getHeader().getPrimaryID(),
                                    URI.create("urn:RobotML:dataflowport"), new URL("http://purl.org/dc/dcmitype/Text"), title, null, null,
                                    creator, new OMMFormat("application/xml", null, null), null, new GenericTypedValue("application/xml", ""),
                                    null, null, null);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        currentMode = ParserMode.DataFlowPort;
                        currentText = currentText.replace(0, currentText.length(), "");
                    }
                    break;

                // serviceport node -> create serviceport block
                case robotMLElementServicePort:
                    if (currentOMM != null) {
//                        System.out.println("---- ---- ... parsing ServicePort '"+attributes.getValue("Name")+"'...");
                        title = new OMMMultiLangText();
                        title.put(Locale.ENGLISH, "ServicePort:"+attributes.getValue("Name"));
                        currentBlock = OMMBlockImpl.create("ServicePort"+ servicePortCounter++, currentOMM.getHeader().getPrimaryID(),
                                URI.create("urn:RobotML:serviceport"), null, title, null, null, creator, new OMMFormat("application/xml", null, null),
                                null, new GenericTypedValue("application/xml", ""), null, null, null);
                        currentMode = ParserMode.ServicePort;
                        currentText = currentText.replace(0, currentText.length(), "");
                    }
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

//        System.out.println("---- characters, in mode: "+currentMode);

        if (!currentMode.equals(ParserMode.General)) {

            if (currentMode.equals(ParserMode.Operation)) {
                for (int i = start; i < start + length; i++) {
                    currentText.append(ch[i]);
                }
            }

            else {
                for (int i = start; i < start + length; i++) {
                    currentText.append(ch[i]);
                }
                if (currentText.length() > 0) {
//                    System.out.println("\tcharacters: "+currentText.toString());
                    currentBlock.setPayload(new GenericTypedValue("text/plain", currentText.toString()), creator);
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

//        System.out.println("---- endElement: "+uri+", "+localName+", "+qName+" | in mode: "+currentMode);

        switch (qName) {

            // component node -> add finished OMM to list
            case robotMLElementComponent:
                parsedOMMs.add(currentOMM);
                currentOMM = null;
                break;

            // attribute node -> add finished block to OMM
            case robotMLElementAttribute:
                if (currentOMM != null) {
                    currentOMM.addBlock(currentBlock, creator);
                    currentMode = ParserMode.General;
                }
                break;

            // operation node -> add finished block to OMM
            case robotMLElementOperation:
                if (currentOMM != null) {
                    currentBlock.setPayload(new GenericTypedValue("application/xml", currentText.toString()), creator);
                    currentOMM.addBlock(currentBlock, creator);
                    currentMode = ParserMode.General;
                }
                break;

            // dataflowport node -> add finished block to OMM
            case robotMLElementDataFlowPort:
                if (currentOMM != null) {
                    currentOMM.addBlock(currentBlock, creator);
                    currentMode = ParserMode.General;
                }
                break;

            // serviceport node -> add finished block to OMM
            case robotMLElementServicePort:
                if (currentOMM != null) {
                    currentBlock.setPayload(new GenericTypedValue("application/xml", currentText.toString()), creator);
                    currentOMM.addBlock(currentBlock, creator);
                    currentMode = ParserMode.General;
                }
                break;

            default:
                break;
        }

        if (currentOMM != null && (currentMode.equals(ParserMode.Operation) || currentMode.equals(ParserMode.ServicePort) )) {
            currentText.append("</");
            currentText.append(qName);
            currentText.append(">");
        }

    }

    @Override
    public void endDocument() {

//        System.out.println("---- end document");

        if (parsedOMMs != null) {

            // create parsed OMMs on the OMS
            for (OMM parsedOMM : parsedOMMs) {
//                System.out.println("creating memory: "+parsedOMM.getHeader().getPrimaryID());
                //String ommName = currentComponentName;
                String ommOwner = OMMFactory.createOMMOwnerStringFromUsernamePassword(cleartextName, username, password);
                OMMBlock ownerBlock = OMMFactory.createOMMOwnerBlock(parsedOMM.getHeader(), ommOwner);
                OMMFactory.cloneOMMViaOMSRestInterface(omsURL+"/mgmt/cloneMemory", parsedOMM, ownerBlock);
            }
        }
    }

    private enum ParserMode
    {
        General, Attribute, Operation, DataFlowPort, ServicePort
    }

}
