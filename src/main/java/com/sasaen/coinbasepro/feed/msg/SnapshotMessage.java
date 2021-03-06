package com.sasaen.coinbasepro.feed.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A snapshot of the order book
 * Example:
 * <pre>
 * {
 *     "type": "snapshot",
 *     "product_id": "BTC-USD",
 *     "bids": [["10101.10", "0.45054140"]],
 *     "asks": [["10102.55", "0.57753524"]]
 * }
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class SnapshotMessage extends FeedMessage {

    private String[][] bids;
    private String[][] asks;

    public SnapshotMessage() {
        setType("snapshot");
    }
}
