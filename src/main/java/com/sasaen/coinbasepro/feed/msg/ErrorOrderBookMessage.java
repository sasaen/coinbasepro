package com.sasaen.coinbasepro.feed.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * If you send a message that is not recognized or an error
 * occurs, the error message will be sent and you will be
 * disconnected.
 * <pre>
 * {
 *     "type": "error",
 *     "message": "error message"
 * }
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ErrorOrderBookMessage extends FeedMessage {

    private String message;
    private String reason;

    public ErrorOrderBookMessage() {
        setType("error");
    }

}
