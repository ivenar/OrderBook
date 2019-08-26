package com.company;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class OrderBook {

    private Map<String, Book> booksByName;
    private Map<Integer, Order> ordersById;

    private class Order {

        private boolean isAsk;
        private BigDecimal price;
        private int volume;
        private int id;

        Order(boolean isAsk, BigDecimal price, int volume, int id) {
            this.isAsk = isAsk;
            this.price = price;
            this.volume = volume;
            this.id = id;
        }

        int getId() {
            return id;
        }

        BigDecimal getPrice() {
            return price;
        }

        int getVolume() {
            return volume;
        }

        void reduceVolume(int amount) {
            volume -= amount;
        }

        boolean isAsk() {
            return isAsk;
        }
    }

    private class XmlOrderHandler extends DefaultHandler {
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            switch (qName) {
                case "AddOrder": {
                    OrderBook.this.addOrder("SELL".equals(attributes.getValue("operation")),
                            attributes.getValue("book"),
                            new BigDecimal(attributes.getValue("price").trim()),
                            Integer.parseInt(attributes.getValue("volume")),
                            Integer.parseInt(attributes.getValue("orderId")));
                    break;
                }
                case "DeleteOrder": {
                    OrderBook.this.deleteOrder(attributes.getValue("book"),
                            Integer.parseInt(attributes.getValue("orderId")));
                    break;
                }
            }
        }
    }

    public OrderBook() {
        booksByName = new HashMap<>();
        ordersById = new HashMap<>();
    }

    public OrderBook(String pathXml) throws ParserConfigurationException, SAXException, IOException {
        this();
        SAXParserFactory.newInstance().newSAXParser().parse(pathXml, new XmlOrderHandler());
    }

    public void addOrder(boolean isAsk, String bookName, BigDecimal price, int volume, int orderId) {
        if (!booksByName.containsKey(bookName)) {
            booksByName.put(bookName, new Book());
        }
        Order order = new Order(isAsk, price, volume, orderId);
        booksByName.get(bookName).addOrder(order);
        if (booksByName.get(bookName).isEmpty()) booksByName.remove(bookName);
    }

    public boolean deleteOrder(String bookName, int orderId) {
        if (!booksByName.containsKey(bookName) || !ordersById.containsKey(orderId)) {
            return false;
        }
        Order order = ordersById.remove(orderId);
        boolean res = booksByName.get(bookName).deleteOrder(orderId, order.getPrice(), order.isAsk());
        if (booksByName.get(bookName).isEmpty()) booksByName.remove(bookName);
        return res;
    }

    public String getReport() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Book> stringBookEntry : booksByName.entrySet()) {
            stringBuilder.append("Стакан: ");
            stringBuilder.append(stringBookEntry.getKey());
            stringBuilder.append("\n");
            stringBuilder.append("Покупка – Продажа\n----------------------------------\nКолво@Цена – Колво@Цена\n");
            List<String> asks = stringBookEntry.getValue().getAsksReport();
            List<String> bids = stringBookEntry.getValue().getBidsReport();
            while (!asks.isEmpty() || !bids.isEmpty()) {
                String bid = bids.isEmpty() ? "---------" : bids.remove(0);
                String ask = asks.isEmpty() ? "---------" : asks.remove(0);
                stringBuilder.append(String.format("%s - %s\n", bid, ask));
            }
            stringBuilder.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        }
        return stringBuilder.toString();
    }

    private class Book {
        private TreeMap<BigDecimal, TreeMap<Integer, Order>> bidPriceLevelsMap;
        private TreeMap<BigDecimal, TreeMap<Integer, Order>> askPriceLevelsMap;

        Book() {
            bidPriceLevelsMap = new TreeMap<>();
            askPriceLevelsMap = new TreeMap<>();
        }

        void addOrder(Order order) {
            Map.Entry<BigDecimal, TreeMap<Integer, Order>> entry = order.isAsk() ? bidPriceLevelsMap.lastEntry() : askPriceLevelsMap.firstEntry();
            while (entry != null && order.getVolume() > 0 && (order.isAsk() ? (entry.getKey().compareTo(order.getPrice()) >= 0) : (entry.getKey().compareTo(order.getPrice()) <= 0))) {
                TreeMap<Integer, Order> orders = entry.getValue();
                while (orders.size() != 0 && order.getVolume() > 0) {
                    Order match = orders.firstEntry().getValue();
                    if (match.getVolume() > order.getVolume()) {
                        match.reduceVolume(order.getVolume());
                        order.reduceVolume(order.getVolume());
                    } else {
                        order.reduceVolume(match.getVolume());
                        orders.remove(orders.firstKey());
                        ordersById.remove(match.getId());
                    }
                }
                if (orders.size() == 0) {
                    if (order.isAsk()) bidPriceLevelsMap.remove(entry.getKey());
                    else askPriceLevelsMap.remove(entry.getKey());
                }
                entry = order.isAsk() ? bidPriceLevelsMap.lastEntry() : askPriceLevelsMap.firstEntry();
            }
            if (order.getVolume() > 0) {
                TreeMap<BigDecimal, TreeMap<Integer, Order>> priceLevelsMap = order.isAsk() ? askPriceLevelsMap : bidPriceLevelsMap;
                if (!priceLevelsMap.containsKey(order.getPrice())) {
                    priceLevelsMap.put(order.getPrice(), new TreeMap<>());
                }
                priceLevelsMap.get(order.getPrice()).put(order.getId(), order);
                ordersById.put(order.getId(), order);
            }
        }

        boolean deleteOrder(int orderId, BigDecimal price, boolean isAsk) {
            Order removedOrder;
            if (isAsk) {
                TreeMap<Integer, Order> map = askPriceLevelsMap.get(price);
                removedOrder = map.remove(orderId);
                if (map.size() == 0) {
                    askPriceLevelsMap.remove(price);
                }
            } else {
                TreeMap<Integer, Order> map = bidPriceLevelsMap.get(price);
                removedOrder = map.remove(orderId);
                if (map.size() == 0) {
                    bidPriceLevelsMap.remove(price);
                }
            }
            return removedOrder != null;
        }

        private List<String> getReport(TreeMap<BigDecimal, TreeMap<Integer, Order>> priceLevelsMap) {
            return priceLevelsMap.entrySet().stream().map(bigDecimalTreeMapEntry -> {
                int sum = 0;
                for (Map.Entry<Integer, Order> entry : bigDecimalTreeMapEntry.getValue().entrySet()) {
                    sum += entry.getValue().getVolume();
                }
                return String.format("%d@%.2f", sum, bigDecimalTreeMapEntry.getKey());
            }).collect(Collectors.toList());
        }

        List<String> getAsksReport() {
            return getReport(askPriceLevelsMap);
        }

        List<String> getBidsReport() {
            List<String> strings = getReport(bidPriceLevelsMap);
            Collections.reverse(strings);
            return strings;
        }

        boolean isEmpty() {
            return (askPriceLevelsMap.size() == 0) && (bidPriceLevelsMap.size() == 0);
        }
    }
}
