package com.hapangama.medibackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for database retry logic with exponential backoff (UC-04 E2)
 */
@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Configure exponential backoff
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1 second
        backOffPolicy.setMultiplier(2.0); // Double each time
        backOffPolicy.setMaxInterval(10000); // Max 10 seconds
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Configure retry policy - retry on specific exceptions
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(SQLException.class, true);
        retryableExceptions.put(org.springframework.dao.DataAccessException.class, true);
        retryableExceptions.put(org.springframework.transaction.TransactionException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
