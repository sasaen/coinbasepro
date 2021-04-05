package com.sasaen.coinbasepro.feed.msg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SubscriptionsMessage.class, name = "subscriptions"),
        @JsonSubTypes.Type(value = SnapshotMessage.class, name = "snapshot"),
        @JsonSubTypes.Type(value = L2UpdateMessage.class, name = "l2update"),
        @JsonSubTypes.Type(value = ErrorOrderBookMessage.class, name = "error")
})
@Data
public abstract class FeedMessage {

    private String type;  // "received" | "open" | "done" | "match" | "change" | "activate"
    private Long sequence;
    private Instant time;
    @JsonProperty("product_id")
    private String productId;
}
