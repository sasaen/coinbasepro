package com.sasaen.coinbasepro.orderbook.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Optional;

@Value
@Builder

public class Order implements Comparable {

    private BigDecimal size;
    private BigDecimal price;

    @Override
    public int compareTo(Object o) {
        if (o instanceof Order) {
            Order other = (Order) o;
            return this.getPrice().compareTo(other.getPrice());
        }
        return -1;
    }
}
