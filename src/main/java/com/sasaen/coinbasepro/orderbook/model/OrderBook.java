package com.sasaen.coinbasepro.orderbook.model;

import lombok.Data;

import java.util.*;

@Data
public class OrderBook {

    private List<Order> bids;
    private List<Order> asks;
    Set<Order> bidsT;
    Set<Order> asksT;
    public OrderBook() {
        asksT = new TreeSet<>(Collections.reverseOrder());
        bidsT = new TreeSet<>();

        this.bids = new ArrayList<>();
        this.asks = new ArrayList<>();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(System.lineSeparator());
        builder.append("asks: ").append(getAsks().size()).append(System.lineSeparator());
        appendListInReverseOrder(getAsks(), builder);
        builder.append("bids: ").append(getBids().size()).append(System.lineSeparator());
        appendList(getBids(), builder);
        return builder.toString();
    }

    private void appendList(List<Order> orderList, StringBuilder builder) {
        orderList.forEach(order -> appendOrder(builder, order));
    }

    private void appendListInReverseOrder(List<Order> orderList, StringBuilder builder) {
        // More optimal just iterating from the last one to the first one
        for (int i = orderList.size() - 1; i >= 0; i--) {
            appendOrder(builder, orderList.get(i));
        }
    }

    private StringBuilder appendOrder(StringBuilder builder, Order order) {
        return builder.append(order.getPrice()).append(", ").append(order.getSize()).append(System.lineSeparator());
    }
}
