package com.sasaen.coinbasepro.feed.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <pre>
 * {
 *     "type": "subscriptions",
 *     "channels": [
 *         {
 *             "name": "level2",
 *             "product_ids": [
 *                 "ETH-USD",
 *                 "ETH-EUR"
 *             ],
 *         },
 *         {
 *             "name": "heartbeat",
 *             "product_ids": [
 *                 "ETH-USD",
 *                 "ETH-EUR"
 *             ],
 *         },
 *         {
 *             "name": "ticker",
 *             "product_ids": [
 *                 "ETH-USD",
 *                 "ETH-EUR",
 *                 "ETH-BTC"
 *             ]
 *         }
 *     ]
 * }
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class SubscriptionsMessage extends FeedMessage {

    private Channel[] channels;
}
