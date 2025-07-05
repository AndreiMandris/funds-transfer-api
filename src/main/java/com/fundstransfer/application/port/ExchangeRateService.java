package com.fundstransfer.application.port;

import java.math.BigDecimal;

public interface ExchangeRateService {

    BigDecimal getExchangeRate(String fromCurrency, String toCurrency);
}