package com.fundstransfer.adapter.frankfurter.client;

import com.fundstransfer.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class FrankfurterExchangeRateServiceTest {

    @Autowired
    private FrankfurterExchangeRateService exchangeRateService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void getExchangeRate_ShouldReturnOne_WhenCurrenciesAreTheSame() {
        // Act
        BigDecimal rate = exchangeRateService.getExchangeRate("USD", "USD");

        // Assert
        assertEquals(BigDecimal.ONE, rate);

        // Verify the result is cached
        Cache.ValueWrapper cachedValue = cacheManager.getCache("exchangeRates").get("USD_USD");
        assertNotNull(cachedValue);
        assertEquals(BigDecimal.ONE, cachedValue.get());
    }

    @Test
    void getExchangeRate_ShouldUseCachedValue_WhenAvailable() {
        // Arrange - Manually put a value in the cache
        String fromCurrency = "EUR";
        String toCurrency = "GBP";
        String cacheKey = fromCurrency + "_" + toCurrency;
        BigDecimal expectedRate = new BigDecimal("0.85");

        // Put the value in the cache
        cacheManager.getCache("exchangeRates").put(cacheKey, expectedRate);

        // Act - This should use the cached value instead of calling the API
        BigDecimal actualRate = exchangeRateService.getExchangeRate(fromCurrency, toCurrency);

        // Assert
        assertEquals(expectedRate, actualRate);
    }
}
