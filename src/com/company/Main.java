package com.company;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            OrderBook orderBook = OrderBookFactory.getOrderBook(args[0]);
            if (orderBook!=null)
                System.out.println(orderBook.getReport());
        } catch (SAXParseException e){
            System.out.println(e.getMessage());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
