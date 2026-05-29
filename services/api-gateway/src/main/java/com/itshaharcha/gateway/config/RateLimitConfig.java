package com.itshaharcha.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimitConfig {

    /** Rate-limit bucket key: authenticated account id when present, else client IP. */
    @Bean
    public KeyResolver rateLimitKeyResolver() {
        return exchange -> {
            String accountId = exchange.getRequest().getHeaders().getFirst("X-Account-Id");
            if (accountId != null && !accountId.isBlank()) {
                return Mono.just(accountId);
            }
            InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
            String ip = remote != null ? remote.getAddress().getHostAddress() : "unknown";
            return Mono.just(ip);
        };
    }
}
