package com.sasaen.coinbasepro.feed.msg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Channel {
    private ChannelName name;

    @JsonProperty("product_ids")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String[] productIds;
}
