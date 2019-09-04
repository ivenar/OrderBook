package com.company;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.math.BigDecimal;

class XmlOrderHandler extends DefaultHandler {

    private OrderBook orderBook;

    public XmlOrderHandler(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {

        switch (qName) {
            case "AddOrder": {
                orderBook.addOrder("SELL".equals(attributes.getValue("operation")),
                        attributes.getValue("book"),
                        new BigDecimal(attributes.getValue("price").trim()),
                        Integer.parseInt(attributes.getValue("volume")),
                        Integer.parseInt(attributes.getValue("orderId")));

                break;
            }
            case "DeleteOrder": {

                orderBook.deleteOrder(attributes.getValue("book"),
                        Integer.parseInt(attributes.getValue("orderId")));

                break;
            }
        }
    }
}
