package com.sasaen.coinbasepro;

import com.sasaen.coinbasepro.feed.WebsocketFeed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;

@SpringBootApplication
public class Application {

    private static String instrument;
    @Autowired
    private WebsocketFeed websocketFeed;

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            instrument = args[0];
            SpringApplication.run(Application.class, args);
        } else {
            System.err.println("Expected an instrument argument. Example:");
            System.err.println("./gradlew bootRun -Pargs=BTC-EUR");
        }
    }

    @PostConstruct
    public void post() throws DeploymentException, IOException, URISyntaxException {
        websocketFeed.subscribe(instrument);
    }
}
