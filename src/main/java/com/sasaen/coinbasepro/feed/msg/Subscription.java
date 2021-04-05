package com.sasaen.coinbasepro.feed.msg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class Subscription {

    private final String type = "subscribe";
    @JsonProperty("product_ids")
    private String[] productIds;
    private Channel[] channels;

    public Subscription(String[] productIds) {
        this.productIds = productIds;
        // Only subscribed to level 2
        this.channels = new Channel[]{new Channel(ChannelName.level2, productIds)};
    }
}
