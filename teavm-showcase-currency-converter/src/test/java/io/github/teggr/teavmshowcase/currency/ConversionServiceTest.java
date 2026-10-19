package io.github.teggr.teavmshowcase.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

class ConversionServiceTest {
    private final ConversionService service = new ConversionService();

    @Test
    void convertsGbpToEuro() {
        ConversionResponse response = service.convert(request("100.00", "GBP", "EUR"));
        assertEquals(new BigDecimal("1.1700000000"), response.rate());
        assertEquals(new BigDecimal("117.00"), response.result());
    }

    @Test
    void convertsInReverseAndBetweenNonGbpCurrencies() {
        assertEquals(new BigDecimal("85.47"),
                service.convert(request("100.00", "EUR", "GBP")).result());
        assertEquals(new BigDecimal("114.53"),
                service.convert(request("100.00", "EUR", "USD")).result());
    }

    @Test
    void roundsHalfUpAndSupportsSameCurrency() {
        assertEquals(new BigDecimal("0.59"),
                service.convert(request("0.50", "GBP", "EUR")).result());
        ConversionResponse sameCurrency = service.convert(request("25.00", "JPY", "JPY"));
        assertEquals(new BigDecimal("25.00"), sameCurrency.result());
        assertTrue(sameCurrency.history().stream().allMatch(point -> point.rate().equals(new BigDecimal("1.0000000000"))));
    }

    @Test
    void returnsIllustrativeHistoryRelativeToTheRequestedPair() {
        ConversionResponse response = service.convert(request("10.00", "EUR", "USD"));
        assertEquals(new BigDecimal("1.1452991453"), response.rate());
        assertEquals(7, response.history().size());
        assertEquals("2026-10-12", response.history().get(0).date());
        assertEquals(response.rate().multiply(new BigDecimal("0.985"))
                .setScale(10, RoundingMode.HALF_UP), response.history().get(0).rate());
        assertEquals(response.rate(), response.history().get(6).rate());
        assertEquals("2026-10-18", response.asOf());
    }

    @Test
    void rejectsInvalidAmountAndCurrency() {
        assertEquals("INVALID_AMOUNT",
                assertThrows(ConversionException.class, () -> service.convert(request("0", "GBP", "EUR"))).getCode());
        assertEquals("AMOUNT_TOO_LARGE",
                assertThrows(ConversionException.class,
                        () -> service.convert(request("1000000.01", "GBP", "EUR"))).getCode());
        assertEquals("INVALID_PRECISION",
                assertThrows(ConversionException.class,
                        () -> service.convert(request("1.001", "GBP", "EUR"))).getCode());
        assertEquals("UNSUPPORTED_CURRENCY",
                assertThrows(ConversionException.class,
                        () -> service.convert(request("1", "CAD", "EUR"))).getCode());
    }

    private static ConversionRequest request(String amount, String from, String to) {
        return new ConversionRequest(new BigDecimal(amount), from, to);
    }
}
