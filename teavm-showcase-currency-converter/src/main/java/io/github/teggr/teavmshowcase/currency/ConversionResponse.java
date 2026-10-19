package io.github.teggr.teavmshowcase.currency;

import java.math.BigDecimal;
import java.util.List;

public record ConversionResponse(
        BigDecimal amount,
        String from,
        String to,
        BigDecimal rate,
        BigDecimal result,
        String asOf,
        List<RateHistoryPoint> history) {
}
