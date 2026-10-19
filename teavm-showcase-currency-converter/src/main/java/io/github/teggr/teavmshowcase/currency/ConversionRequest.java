package io.github.teggr.teavmshowcase.currency;

import java.math.BigDecimal;

import tools.jackson.databind.annotation.JsonDeserialize;

public class ConversionRequest {
    @JsonDeserialize(using = StrictBigDecimalDeserializer.class)
    private BigDecimal amount;
    private String from;
    private String to;

    public ConversionRequest() {
    }

    public ConversionRequest(BigDecimal amount, String from, String to) {
        this.amount = amount;
        this.from = from;
        this.to = to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
