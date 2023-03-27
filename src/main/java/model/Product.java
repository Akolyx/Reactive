package model;

import org.bson.Document;

public class Product {
    private final String name;
    private final double value;

    private final Currency currency;

    public Product(String name, double value, Currency currency) {
        this.name = name;
        this.value = value;
        this.currency = currency;
    }

    public Product(Document document) {
        this(document.getString("name"),
                document.getDouble("value"),
                Currency.valueOf(document.getString("currency")));
    }

    @Override
    public String toString() {
        return "Product {\n" +
                "  name : " + name + ",\n" +
                "  value : " + value + ",\n" +
                "  currency : " + currency + "\n" +
                "}";
    }

    public Document toDocument() {
        return new Document()
                .append("name", name)
                .append("value", value)
                .append("currency", currency.toString());
    }

    public Product convertCurrency(Currency otherCurrency) {
        return new Product(name, value * currency.getMultiplier(otherCurrency), otherCurrency);
    }
}
