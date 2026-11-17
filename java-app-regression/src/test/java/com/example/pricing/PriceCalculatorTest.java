package com.example.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorTest {

    private final PriceCalculator calculator = new PriceCalculator();

    @Test
    void appliesTwentyPercentDiscountCorrectly() {
        double result = calculator.applyDiscount(100.0, 20.0);
        assertEquals(80.0, result, 0.0001);
    }

    @Test
    void appliesTenPercentDiscountCorrectly() {
        double result = calculator.applyDiscount(50.0, 10.0);
        assertEquals(45.0, result, 0.0001);
    }

    @Test
    void zeroPercentDiscountLeavesPriceUnchanged() {
        double result = calculator.applyDiscount(30.0, 0.0);
        assertEquals(30.0, result, 0.0001);
    }
}
