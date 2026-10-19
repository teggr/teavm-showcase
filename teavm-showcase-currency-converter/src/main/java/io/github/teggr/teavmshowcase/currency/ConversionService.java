package io.github.teggr.teavmshowcase.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ConversionService {
    public ConversionResponse convert(ConversionRequest request) {
        if (request == null) {
            throw new ConversionException("INVALID_REQUEST", "A conversion request is required.");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new ConversionException("INVALID_AMOUNT", "Amount must be greater than zero.");
        }
        if (amount.scale() > ExchangeRates.MAX_SCALE) {
            throw new ConversionException("INVALID_PRECISION", "Amount may have no more than 2 decimal places.");
        }
        if (amount.compareTo(ExchangeRates.MAX_AMOUNT) > 0) {
            throw new ConversionException("AMOUNT_TOO_LARGE", "Amount must not exceed 1,000,000.");
        }

        BigDecimal fromRate = requireRate(request.getFrom());
        BigDecimal toRate = requireRate(request.getTo());
        BigDecimal rate = toRate.divide(fromRate, ExchangeRates.RATE_SCALE, RoundingMode.HALF_UP);
        BigDecimal result = amount.multiply(toRate).divide(fromRate, 2, RoundingMode.HALF_UP);

        List<RateHistoryPoint> history = rateHistory(request.getFrom(), request.getTo(), rate);
        return new ConversionResponse(amount, request.getFrom(), request.getTo(), rate, result,
                ExchangeRates.AS_OF, history);
    }

    private static List<RateHistoryPoint> rateHistory(String from, String to, BigDecimal rate) {
        LocalDate asOf = LocalDate.parse(ExchangeRates.AS_OF);
        List<RateHistoryPoint> history = new ArrayList<>();
        for (int i = 0; i < ExchangeRates.DEMO_RATE_FACTORS.size(); ++i) {
            LocalDate date = asOf.minusDays(ExchangeRates.DEMO_RATE_FACTORS.size() - i - 1);
            BigDecimal pointRate = from.equals(to)
                    ? BigDecimal.ONE.setScale(ExchangeRates.RATE_SCALE)
                    : rate.multiply(new BigDecimal(ExchangeRates.DEMO_RATE_FACTORS.get(i)))
                            .setScale(ExchangeRates.RATE_SCALE, RoundingMode.HALF_UP);
            history.add(new RateHistoryPoint(date.toString(), pointRate));
        }
        return List.copyOf(history);
    }

    private static BigDecimal requireRate(String currency) {
        BigDecimal rate = currency == null ? null : ExchangeRates.unitsPerGbp(currency);
        if (rate == null) {
            throw new ConversionException("UNSUPPORTED_CURRENCY", "Choose one of the supported currencies.");
        }
        return rate;
    }
}
