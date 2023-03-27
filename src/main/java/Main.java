import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import dao.CatalogDao;
import dao.UserDao;
import rx.Observable;
import http.RxCatalogServer;
import io.reactivex.netty.protocol.http.server.HttpServer;


public class Main {
    public static void main(String[] args) {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        UserDao userDao = new UserDao(client);
        CatalogDao catalogDao = new CatalogDao(client);
        RxCatalogServer server = new RxCatalogServer(userDao, catalogDao);
        HttpServer
                .newServer(8080)
                .start((req, resp) -> {
                    Observable<String> response = server.getResponse(req);
                    return resp.writeString(response.map(s -> s + System.lineSeparator()));
                })
                .awaitShutdown();
    }
}