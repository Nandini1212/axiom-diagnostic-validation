package com.example.pricing;

public class PriceCalculator {

    public double applyDiscount(double price, double percentOff) {
        if (percentOff < 0 || percentOff > 100) {
            throw new IllegalArgumentException("percentOff must be between 0 and 100");
        }
        return price - (price * percentOff / 100);
    }
}
