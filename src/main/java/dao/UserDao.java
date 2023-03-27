package dao;

import com.mongodb.client.model.Filters;
import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoCollection;
import com.mongodb.rx.client.Success;
import model.User;
import org.bson.Document;
import rx.Observable;
import model.Currency;

public class UserDao {
    MongoCollection<Document> userCollection;
    static final String COLLECTION_NAME = "users";

    public UserDao(MongoClient client) {
        userCollection = client.getDatabase("catalog").getCollection(COLLECTION_NAME);
    }

    public Observable<Success> registerUser(int id, String currencyString, String name) {
        return userCollection.find(Filters.eq("id", id))
                .toObservable()
                .isEmpty()
                .flatMap(isEmpty -> {
                    if (isEmpty) {
                        Currency currency = Currency.valueOf(currencyString);
                        return userCollection.insertOne(new User(id, name, currency).toDocument());
                    } else {
                        return Observable.error(new IllegalArgumentException("User with id '" + id + "' already exists"));
                    }
                });
    }

    public Observable<User> getUserById(int id) {
        return userCollection.find(Filters.eq("id", id))
                .toObservable()
                .map(User::new);

    }


}
