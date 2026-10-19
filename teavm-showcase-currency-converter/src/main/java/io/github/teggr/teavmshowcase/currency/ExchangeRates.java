package io.github.teggr.teavmshowcase.currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class ExchangeRates {
    public static final String AS_OF = "2026-10-18";
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000.00");
    public static final int MAX_SCALE = 2;
    public static final int RATE_SCALE = 10;
    public static final List<String> DEMO_RATE_FACTORS =
            List.of("0.985", "0.992", "0.989", "1.004", "1.012", "1.006", "1.000");

    private static final Map<String, BigDecimal> UNITS_PER_GBP = Map.of(
            "GBP", new BigDecimal("1.00"),
            "EUR", new BigDecimal("1.17"),
            "USD", new BigDecimal("1.34"),
            "JPY", new BigDecimal("201.50"));

    private ExchangeRates() {
    }

    public static BigDecimal unitsPerGbp(String currency) {
        return UNITS_PER_GBP.get(currency);
    }
}
