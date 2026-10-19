package io.github.teggr.teavmshowcase.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AmountValidatorTest {
    @Test
    void normalizesValidAmountsWithoutFloatingPointConversion() {
        assertEquals("100", AmountValidator.normalize(" 00100 "));
        assertEquals("0.05", AmountValidator.normalize("0.05"));
        assertEquals("1000000.0", AmountValidator.normalize("1000000.0"));
        assertEquals("1000000.00", AmountValidator.normalize("1000000.00"));
    }

    @Test
    void rejectsInvalidAndOutOfRangeAmounts() {
        assertNull(AmountValidator.normalize(""));
        assertNull(AmountValidator.normalize("0"));
        assertNull(AmountValidator.normalize("0.00"));
        assertNull(AmountValidator.normalize("-1"));
        assertNull(AmountValidator.normalize("1e2"));
        assertNull(AmountValidator.normalize("1.001"));
        assertNull(AmountValidator.normalize("1000000.01"));
        assertNull(AmountValidator.normalize("1000001"));
    }
}
