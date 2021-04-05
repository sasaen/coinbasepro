package com.sasaen.coinbasepro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("coinbasepro.exchange")
public class ExchangeConfig {

    private String wsUrl;
    private Integer orderBookLevels;
    private Integer orderBookPrintLevels;
}