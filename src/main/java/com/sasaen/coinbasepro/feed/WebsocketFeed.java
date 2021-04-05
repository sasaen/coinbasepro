package com.sasaen.coinbasepro.feed;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sasaen.coinbasepro.config.ExchangeConfig;
import com.sasaen.coinbasepro.feed.msg.ErrorOrderBookMessage;
import com.sasaen.coinbasepro.feed.msg.FeedMessage;
import com.sasaen.coinbasepro.feed.msg.Subscription;
import com.sasaen.coinbasepro.orderbook.OrderBookService;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@ClientEndpoint
@Service
public class WebsocketFeed {

    private final ExchangeConfig exchangeConfig;
    private final ObjectMapper objectMapper;
    private final OrderBookService orderBookService;
    private final RetryPolicy<Object> retryPolicy;
    private final ExecutorService executorService;

    private Session userSession;
    private String instrument;

    public WebsocketFeed(ExchangeConfig exchangeConfig, OrderBookService orderBookService) {
        this.exchangeConfig = exchangeConfig;
        this.orderBookService = orderBookService;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.retryPolicy = new RetryPolicy<>()
                .handle(Exception.class)
                .withDelay(Duration.ofSeconds(1))
                .withMaxRetries(-1);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void subscribe(String instrument) throws URISyntaxException, IOException, DeploymentException {
        this.instrument = instrument;

        connectToWsServer();
        sendSubscription(instrument);
    }

    private void connectToWsServer() throws DeploymentException, IOException, URISyntaxException {
        log.info("Connecting to websocket {}", exchangeConfig.getWsUrl());
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxBinaryMessageBufferSize(1024 * 1024);
        container.setDefaultMaxTextMessageBufferSize(1024 * 1024);
        container.connectToServer(this, new URI(exchangeConfig.getWsUrl()));
    }

    @OnMessage
    public void onMessage(String json) {
        executorService.submit(() -> {
            try {
                FeedMessage feedMessage = objectMapper.readValue(json, FeedMessage.class);
                if (feedMessage == null) {
                    log.error("Received null feedMessage  {}", json);
                    return;
                }
                if (feedMessage instanceof ErrorOrderBookMessage) {
                    log.error("Failed to subscribe {}", ((ErrorOrderBookMessage) feedMessage).getReason());
                    System.exit(-1);
                }

                orderBookService.handleFeedMessage(feedMessage);
            } catch (Exception e) {
                log.error("Error parsing message {}", e);
            }
        });
    }

    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) throws IOException {
        log.error("closing websocket reason={} ", reason);
        this.userSession.close();
        this.userSession = null;

        Failsafe.with(retryPolicy).run(() -> reconect());
    }

    @OnError
    public void onError(Throwable t) {
        log.error("websocket error", t);
    }

    private void reconect() throws DeploymentException, IOException, URISyntaxException {
        log.warn("Attempting to reconnect websocket ");
        subscribe(instrument);
        log.warn("reconnected websocket ");
    }

    private void sendSubscription(String instrument) throws JsonProcessingException {
        log.info("Subscribing to instrument {}", instrument);
        String jsonSubscribeMessage = signObject(new Subscription(new String[]{instrument}));
        this.userSession.getAsyncRemote().sendText(jsonSubscribeMessage);
    }

    private String signObject(Subscription jsonObj) throws JsonProcessingException {
        return toJson(jsonObj);
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
