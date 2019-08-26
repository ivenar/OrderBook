package com.company;

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            OrderBook orderBook = new OrderBook("orders.xml");
            System.out.println(orderBook.getReport());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
