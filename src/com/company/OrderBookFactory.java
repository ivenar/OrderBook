package com.company;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class OrderBookFactory {

    public static OrderBook getOrderBook(String pathXml)throws ParserConfigurationException, SAXException, IOException {
        OrderBook orderBook = new OrderBook();
        Schema schema = null;
        try {
            String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            SchemaFactory factory = SchemaFactory.newInstance(language);
            schema = factory.newSchema(new File("resourses/Schema.xsd"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Validator validator = schema.newValidator();
        SAXSource saxSource = new SAXSource(new InputSource(new FileInputStream(pathXml)));
        OrderBookErrorHandler orderBookErrorHandler = new OrderBookErrorHandler();
        validator.setErrorHandler(orderBookErrorHandler);
        validator.validate(saxSource);
        if (orderBookErrorHandler.hadException()){
            for (Exception e:orderBookErrorHandler.getSaxParseExceptions()) {
                System.err.println(e.getMessage());
            }
            return null;
        }

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.parse(pathXml, new XmlOrderHandler(orderBook));
        return orderBook;
    }
}
