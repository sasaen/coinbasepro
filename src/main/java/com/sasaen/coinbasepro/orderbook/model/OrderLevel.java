package com.sasaen.coinbasepro.orderbook.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder

public class OrderLevel implements Comparable {

    private BigDecimal marketSize;
    private BigDecimal price;

    @Override
    public int compareTo(Object o) {
        if (o instanceof OrderLevel) {
            OrderLevel other = (OrderLevel) o;
            return this.getPrice().compareTo(other.getPrice());
        }
        return -1;
    }
}
