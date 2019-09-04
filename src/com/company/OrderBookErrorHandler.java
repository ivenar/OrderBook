package com.company;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

public class OrderBookErrorHandler implements ErrorHandler {

    final private List<SAXParseException> saxParseExceptions = new ArrayList<>();
    @Override
    public void warning(SAXParseException exception) throws SAXException {
        saxParseExceptions.add(exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        saxParseExceptions.add(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        saxParseExceptions.add(exception);
    }

    public List<SAXParseException> getSaxParseExceptions() {
        return saxParseExceptions;
    }

    public boolean hadException(){
        return saxParseExceptions.size()>0;
    }
}
