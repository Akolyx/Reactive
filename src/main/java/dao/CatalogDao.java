package dao;
import com.mongodb.client.model.Filters;
import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoCollection;
import com.mongodb.rx.client.Success;
import model.Product;
import org.bson.Document;
import rx.Observable;
import model.Currency;

public class CatalogDao {
    MongoCollection<Document> itemsCollection;
    static final String COLLECTION_NAME = "items";

    public CatalogDao(MongoClient client) {
        itemsCollection = client.getDatabase("catalog").getCollection(COLLECTION_NAME);
    }

    public Observable<Success> addProduct(String name, int value, String currencyString) {
        return itemsCollection.find(Filters.eq("name", name))
                .toObservable()
                .isEmpty()
                .flatMap(isEmpty -> {
                    if (isEmpty) {
                        Currency currency = Currency.valueOf(currencyString);
                        return itemsCollection.insertOne(new Product(name, value, currency).toDocument());
                    } else {
                        return Observable.error(new IllegalArgumentException("Item with name '" + name + "' is already exists"));
                    }
                });
    }

    public Observable<Product> getProducts() {
        return itemsCollection.find()
                .toObservable()
                .map(Product::new);

    }
}
