package model;

public enum Currency {
    USD,
    EUR,
    RUB;

    private double getMultiplier() {
        return switch (this) {
            case EUR -> 0.93;
            case RUB -> 76.54;
            default -> 1;
        };
    }

    public double getMultiplier(Currency otherCurrency) {
        return otherCurrency.getMultiplier() / getMultiplier();
    }
}
