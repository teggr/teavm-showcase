package io.github.teggr.teavmshowcase.currency;

import java.math.BigDecimal;

public record RateHistoryPoint(String date, BigDecimal rate) {
}
