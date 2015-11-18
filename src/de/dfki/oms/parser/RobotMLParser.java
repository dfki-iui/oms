package de.dfki.oms.parser;

import de.dfki.omm.impl.OMMFactory;
import de.dfki.omm.impl.OMMImpl;
import de.dfki.omm.interfaces.OMMBlock;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

/**
 * Parser to convert RobotML documents to OMMs.
 *
 * Created by xekl01 on 25.09.2015.
 */
public class RobotMLParser {

    private static String defaultOmsUrl = "http://localhost:10082";

    /**
     * Main method, asks for a RobotML file and parses it to the server.
     */
    public static void main (String[] args) {

        //System.out.println("starting main");

        // get RobotML file
        File robotMLFile = null;
        JFrame dummyFrame = new JFrame();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("XML Strings (.xml or .txt)", "xml", "txt"));
        if (fileChooser.showOpenDialog(dummyFrame) == JFileChooser.APPROVE_OPTION)
            robotMLFile = fileChooser.getSelectedFile();

        // parse file
        parseRobotMLFile(robotMLFile);

        // clean up
        dummyFrame.dispatchEvent(new WindowEvent(dummyFrame, WindowEvent.WINDOW_CLOSING));
        dummyFrame.dispose();
        //System.out.println("ending main");

    }

    /**
     * Parses a RobotML file and adds its components as OMMs to the OMS given by a URL.
     * @param robotMLFile File containing the RobotML scenario description.
     * @param omsUrl Host and port of the OMS to use.
     */
    public static void parseRobotMLFile (File robotMLFile, String omsUrl) {

        RobotMLContentHandler robotMLHandler = new RobotMLContentHandler(omsUrl);

        if (robotMLFile != null)
            try { // parse file (OMMs are created and added in the handler)
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(true);
                SAXParser saxParser = spf.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();
                xmlReader.setContentHandler(robotMLHandler);
                xmlReader.parse(robotMLFile.getAbsolutePath());
            } catch (ParserConfigurationException|SAXException|IOException e) {
                e.printStackTrace();
            }

        else System.out.println("No file selected.");

    }

    /**
     * Parses a RobotML file and adds its components as OMMs to the OMS, using the default URL.
     * @param robotMLFile File containing the RobotML scenario description.
     */
    public static void parseRobotMLFile (File robotMLFile) {
        parseRobotMLFile(robotMLFile, defaultOmsUrl);
    }

    /**
     * Parses a RobotML String and adds its components as OMMs to the OMS given by a URL.
     * @param robotMLString String containing the RobotML scenario description.
     * @param omsUrl Host and port of the OMS to use.
     */
    public static void parseRobotMLString (String robotMLString, String omsUrl) {

        RobotMLContentHandler robotMLHandler = new RobotMLContentHandler(omsUrl);

        if (robotMLString != null)
            try { // parse file (OMMs are created and added in the handler)
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(true);
                SAXParser saxParser = spf.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();
                xmlReader.setContentHandler(robotMLHandler);
                //xmlReader.parse(robotMLFile.getAbsolutePath());
                xmlReader.parse(new InputSource(new StringReader(robotMLString)));

            } catch (ParserConfigurationException|SAXException|IOException e) {
                e.printStackTrace();
            }

        else System.out.println("No RobotML String.");

    }

    /**
     * Parses a RobotML String and adds its components as OMMs to the OMS, using the default URL.
     * @param robotMLString String containing the RobotML scenario description.
     */
    public static void parseRobotMLString (String robotMLString) {
        parseRobotMLString(robotMLString, defaultOmsUrl);
    }

}
