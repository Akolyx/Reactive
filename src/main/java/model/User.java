package model;

import org.bson.Document;

public class User {
    private final int id;
    private final String name;
    private final Currency currency;

    public User(int id, String name, Currency currency) {
        this.id = id;
        this.name = name;
        this.currency = currency;
    }

    public User(Document document){
        this(document.getInteger("id"),
                document.getString("name"),
                Currency.valueOf(document.getString("currency")));
    }

    public Document toDocument() {
        return new Document()
                .append("id", id)
                .append("name", name)
                .append("currency", currency.toString());
    }

    public Currency getCurrency() {
        return currency;
    }
}
