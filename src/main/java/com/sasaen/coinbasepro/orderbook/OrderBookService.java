package com.sasaen.coinbasepro.orderbook;

import com.sasaen.coinbasepro.config.ExchangeConfig;
import com.sasaen.coinbasepro.feed.msg.FeedMessage;
import com.sasaen.coinbasepro.feed.msg.L2UpdateMessage;
import com.sasaen.coinbasepro.feed.msg.SnapshotMessage;
import com.sasaen.coinbasepro.orderbook.model.OrderBook;
import com.sasaen.coinbasepro.orderbook.model.OrderLevel;
import com.sasaen.coinbasepro.util.PrintSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderBookService {

    private final Map<String, OrderBook> orderBookMap = new HashMap<>();
    private final ExchangeConfig exchangeConfig;

    public synchronized void handleFeedMessage(FeedMessage feedMessage) {
        if (feedMessage instanceof SnapshotMessage) {
            buildOrderBook((SnapshotMessage) feedMessage);
        } else if (feedMessage instanceof L2UpdateMessage) {
            L2UpdateMessage l2UpdateMessage = (L2UpdateMessage) feedMessage;
            Optional.ofNullable(orderBookMap.get(l2UpdateMessage.getProductId()))
                    .ifPresentOrElse(orderBook -> handleL2Update(l2UpdateMessage, orderBook),
                            () -> log.error("Order book not found for product id: {}", l2UpdateMessage.getProductId()));
        } else {
            log.info("Received message {} ", feedMessage);
        }
    }

    private void buildOrderBook(SnapshotMessage orderBookResponse) {
        log.info("Received order book snapshot  {}", orderBookResponse.getProductId());

        OrderBook orderBook = new OrderBook();
        populateOrderLevelSet(orderBookResponse.getBids(), orderBook.getBids());
        populateOrderLevelSet(orderBookResponse.getAsks(), orderBook.getAsks());

        orderBookMap.put(orderBookResponse.getProductId(), orderBook);
    }

    private void populateOrderLevelSet(String[][] snapshotArray, Set<OrderLevel> bids) {
        Arrays.stream(snapshotArray)
                .limit(this.exchangeConfig.getOrderBookLevels())
                .map(strings -> this.buildOrder(strings[0], strings[1]))
                .forEach(bid -> bids.add(bid));
    }

    private void handleL2Update(L2UpdateMessage l2UpdateMessage, OrderBook orderBook) {
        log.debug("Received l2 update  {}", l2UpdateMessage);

        String[][] changes = l2UpdateMessage.getChanges();
        for (int i = 0; i < changes.length; i++) {
            if ("buy".equals(changes[i][0])) {
                handleUpdate(orderBook, orderBook.getBids(), changes[i]);
            } else {
                handleUpdate(orderBook, orderBook.getAsks(), changes[i]);
            }
        }
    }

    private void handleUpdate(OrderBook orderBook, NavigableSet<OrderLevel> orderLevelSet, String[] change) {
        OrderLevel updatedOrderLevel = buildOrder(change[1], change[2]);
        if (shouldUpdateOrderBook(orderLevelSet, updatedOrderLevel)) {
            updateOrderBook(orderLevelSet, updatedOrderLevel);
            printOrderBook(orderBook);
        }
    }

    private boolean shouldUpdateOrderBook(NavigableSet<OrderLevel> orderLevelSet,
                                          OrderLevel updatedOrderLevel) {
        if (orderLevelSet.size() < exchangeConfig.getOrderBookLevels() || orderLevelSet.contains(updatedOrderLevel)) {
            return true;
        }

        return orderLevelSet.higher(updatedOrderLevel) != null;
    }

    private void updateOrderBook(NavigableSet<OrderLevel> orderLevelSet,
                                 OrderLevel updatedOrderLevel) {

        if (updatedOrderLevel.getMarketSize().compareTo(BigDecimal.ZERO) == 0) {
            removeFromOrderBook(orderLevelSet, updatedOrderLevel);
        } else {
            if (orderLevelSet.contains(updatedOrderLevel)) {
                updateInOrderBook(orderLevelSet, updatedOrderLevel);
            } else {
                addToOrderBook(orderLevelSet, updatedOrderLevel);
            }
        }
    }

    private void updateInOrderBook(NavigableSet<OrderLevel> orderLevelSet,
                                   OrderLevel updatedOrderLevel) {
        orderLevelSet.remove(updatedOrderLevel);
        orderLevelSet.add(updatedOrderLevel);
    }

    private void addToOrderBook(NavigableSet<OrderLevel> orderLevelSet,
                                OrderLevel updatedOrderLevel) {
        orderLevelSet.add(updatedOrderLevel);
        checkOrderLevelSize(orderLevelSet);
    }

    private void checkOrderLevelSize(NavigableSet<OrderLevel> orderLevelSet) {
        if (orderLevelSet.size() > exchangeConfig.getOrderBookLevels()) {
            orderLevelSet.pollLast();
        }
    }

    private void removeFromOrderBook(NavigableSet<OrderLevel> orderLevelSet,
                                     OrderLevel updatedOrderLevel) {
        orderLevelSet.remove(updatedOrderLevel);
    }

    private OrderLevel buildOrder(String price, String size) {
        return OrderLevel.builder()
                .price(new BigDecimal(price))
                .marketSize(new BigDecimal(size))
                .build();
    }

    private void printOrderBook(OrderBook orderBook) {
        PrintSupport.printOrderBook(orderBook, exchangeConfig.getOrderBookPrintLevels());
    }
}
