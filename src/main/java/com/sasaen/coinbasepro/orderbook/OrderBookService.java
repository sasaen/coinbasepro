package com.sasaen.coinbasepro.orderbook;

import com.sasaen.coinbasepro.config.ExchangeConfig;
import com.sasaen.coinbasepro.feed.msg.FeedMessage;
import com.sasaen.coinbasepro.feed.msg.L2UpdateMessage;
import com.sasaen.coinbasepro.feed.msg.SnapshotMessage;
import com.sasaen.coinbasepro.orderbook.model.Order;
import com.sasaen.coinbasepro.orderbook.model.OrderBook;
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
        String type = feedMessage.getType();
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
        orderBookMap.put(orderBookResponse.getProductId(), orderBook);

        List<Order> bids = orderBook.getBids();
        Arrays.stream(orderBookResponse.getBids())
                .limit(this.exchangeConfig.getOrderBookLevels())
                .map(strings -> this.buildOrder(strings[0], strings[1]))
                .forEach(bid -> bids.add(bid));

        List<Order> asks = orderBook.getAsks();
        Arrays.stream(orderBookResponse.getAsks())
                .limit(this.exchangeConfig.getOrderBookLevels())
                .map(strings -> this.buildOrder(strings[0], strings[1]))
                .forEach(ask -> asks.add(ask));
    }

    private void handleL2Update(L2UpdateMessage l2UpdateMessage, OrderBook orderBook) {
        log.debug("Received l2 update  {}", l2UpdateMessage);

        String[][] changes = l2UpdateMessage.getChanges();
        for (int i = 0; i < changes.length; i++) {
            if ("buy".equals(changes[i][0])) {
                handleUpdate(orderBook, orderBook.getBids(), changes[i], true);
            } else {
                log.info("Received l2 update  {}", l2UpdateMessage);
                handleUpdate(orderBook, orderBook.getAsks(), changes[i], false);
            }
        }
    }

    private void handleUpdate(OrderBook orderBook, List<Order> orderList, String[] change, boolean isBuy) {
        BigDecimal changePrice = new BigDecimal(change[1]);

        if (shouldUpdateOrderBook(changePrice, orderList, isBuy)) {
            updateOrderBook(change, changePrice, orderList, isBuy);
            printOrderBook(orderBook);
        }
    }

    private boolean shouldUpdateOrderBook(BigDecimal price, List<Order> orderList, boolean isBuy) {
        int result = compareToLastOrder(price, orderList);
        return result == expectedResult(isBuy) || result == 0;
    }

    private int expectedResult(boolean isBuy) {
        return isBuy ? 1 : -1;
    }

    private int compareToLastOrder(BigDecimal price, List<Order> orderList) {
        if (orderList.isEmpty()) {
            return 0;
        }
        Order lastOrder = orderList.get(orderList.size() - 1);
        return price.compareTo(lastOrder.getPrice());
    }

    private void updateOrderBook(String[] change, BigDecimal changePrice, List<Order> orderList, boolean isBuy) {
        // Find the position for the new order by iterating the list of orders
        int pos = 0;
        boolean existsInOrderBook = false;
        while (pos < orderList.size() - 1) {
            BigDecimal orderBookPrice = orderList.get(pos).getPrice();
            int compareTo = changePrice.compareTo(orderBookPrice);
            if (compareTo == 0) {
                existsInOrderBook = true;
                break;
            } else {
                if (compareTo != expectedResult(isBuy)) {
                    pos++;
                } else {
                    break;
                }
            }
        }

        Order newOrder = buildOrder(changePrice, change[2]);
        if (newOrder.getSize().compareTo(BigDecimal.ZERO) == 0) {
            removeFromOrderBook(orderList, pos);
        } else {
            if (existsInOrderBook) {
                updateInOrderBook(orderList, pos, newOrder);
            } else {
                addToOrderBook(orderList, pos, newOrder);
            }
        }
    }

    private void updateInOrderBook(List<Order> orderList, int pos, Order newOrder) {
        orderList.set(pos, newOrder);
    }

    private void addToOrderBook(List<Order> orderList, int pos, Order newOrder) {
        orderList.add(pos, newOrder);
        if (orderList.size() > exchangeConfig.getOrderBookLevels()) {
            orderList.remove(exchangeConfig.getOrderBookLevels().intValue());
        }
    }

    private void removeFromOrderBook(List<Order> orderList, int pos) {
        orderList.remove(pos);
    }

    private Order buildOrder(String price, String size) {
        return Order.builder()
                .price(new BigDecimal(price))
                .size(new BigDecimal(size))
                .build();
    }

    private Order buildOrder(BigDecimal price, String size) {
        return Order.builder()
                .price(price)
                .size(new BigDecimal(size))
                .build();
    }

    private void printOrderBook(OrderBook orderBook) {
        log.info("Updated orderBook");
        log.info("{}", orderBook);
    }
}
