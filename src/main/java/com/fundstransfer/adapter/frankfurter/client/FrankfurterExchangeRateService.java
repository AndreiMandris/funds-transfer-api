package com.fundstransfer.adapter.frankfurter.client;

import com.fundstransfer.application.port.ExchangeRateService;
import com.fundstransfer.domain.model.exception.ExchangeRateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class FrankfurterExchangeRateService implements ExchangeRateService {

    private final WebClient webClient;
    private final Duration timeout;

    public FrankfurterExchangeRateService(
            @Value("${app.exchange-rate.api.base-url}") String baseUrl,
            @Value("${app.exchange-rate.api.timeout}") int timeoutMs) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "#fromCurrency + '_' + #toCurrency")
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        try {
            ExchangeRateResponse response = webClient.get()
                    .uri("/latest?from={from}&to={to}", fromCurrency, toCurrency)
                    .retrieve()
                    .bodyToMono(ExchangeRateResponse.class)
                    .timeout(timeout)
                    .block();

            if (response == null || response.rates() == null || !response.rates().containsKey(toCurrency)) {
                throw new ExchangeRateException("Unable to retrieve exchange rate for " + fromCurrency + " to " + toCurrency);
            }

            return response.rates().get(toCurrency);

        } catch (WebClientResponseException e) {
            throw new ExchangeRateException("Failed to retrieve exchange rate: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ExchangeRateException("Unexpected error retrieving exchange rate", e);
        }
    }

    public record ExchangeRateResponse(
            String base,
            String date,
            java.util.Map<String, BigDecimal> rates
    ) {
    }
} 
