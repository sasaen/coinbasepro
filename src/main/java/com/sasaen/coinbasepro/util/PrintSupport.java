package com.sasaen.coinbasepro.util;

import com.sasaen.coinbasepro.orderbook.model.OrderBook;
import com.sasaen.coinbasepro.orderbook.model.OrderLevel;
import lombok.extern.slf4j.Slf4j;

import java.util.NavigableSet;

@Slf4j
public class PrintSupport {

    public static void printOrderBook(OrderBook orderBook, int levels) {
        log.info("Updated orderBook");
        log.info("");

        log.info("asks: {}", orderBook.getAsks().size());
        printOrderLevelsDescendingOrder(orderBook.getAsks(), levels);
        log.info("");
        log.info("bids: {}", orderBook.getBids().size());
        printOrderLevels(orderBook.getBids(), levels);
        log.info("");
    }

    private static void printOrderLevels(NavigableSet<OrderLevel> orderLevelSet, int levels) {
        orderLevelSet.stream().limit(levels).forEach(PrintSupport::printOrderLevel);
    }

    private static void printOrderLevelsDescendingOrder(NavigableSet<OrderLevel> orderLevelSet, int levels) {
        int currentLevel = orderLevelSet.size();
        for (OrderLevel orderLevel : orderLevelSet.descendingSet()) {
            if (currentLevel <= levels) {
                printOrderLevel(orderLevel);
            }
            currentLevel--;
        }
    }

    private static void printOrderLevel(OrderLevel orderLevel) {
        log.info("{}, {}", orderLevel.getPrice(), orderLevel.getMarketSize());
    }
}
