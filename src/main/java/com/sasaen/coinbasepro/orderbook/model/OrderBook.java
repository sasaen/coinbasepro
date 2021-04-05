package com.sasaen.coinbasepro.orderbook.model;

import lombok.Data;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;

@Data
public class OrderBook {

    private NavigableSet<OrderLevel> bids;
    private NavigableSet<OrderLevel> asks;

    public OrderBook() {
        asks = new TreeSet<>();
        bids = new TreeSet<>(Collections.reverseOrder());
    }
}
