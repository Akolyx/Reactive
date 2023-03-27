package http;

import dao.*;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import model.Product;
import model.User;
import rx.Observable;

import java.util.*;
import java.util.stream.Collectors;

public class RxCatalogServer {
    private final UserDao userDao;
    private final CatalogDao catalogDao;

    public RxCatalogServer(UserDao userDao, CatalogDao catalogDao) {
        this.userDao = userDao;
        this.catalogDao = catalogDao;
    }

    public Observable<String> getResponse(HttpServerRequest<ByteBuf> req) {
        String path = req.getDecodedPath().substring(1);
        return switch (path) {
            case "add_product" -> addProduct(req);
            case "register" -> register(req);
            case "catalog" -> showCatalog(req);
            default -> Observable.just("Unknown request: '" + path + "'.Known commands:\n" +
                    "add_product\n\tParameters: name, value, currency\n\tInfo: add item with given name into catalog with value in currency\n" +
                    "register\n\tParameters: id, name, currency\n\tInfo: register user with such id and name to display items in catalog in given currency\n" +
                    "catalog\n\tParameters: id\n\tInfo: shows items in catalog for given user in currency, that was selected while registration\n"
            );
        };
    }

    private Observable<String> showCatalog(HttpServerRequest<ByteBuf> req) {
        Optional<String> error = checkRequestParameters(req, List.of("id"));

        if (error.isPresent()) {
            return Observable.just(error.get());
        }

        Map<String, List<String>> queryParameters = req.getQueryParameters();
        int id = Integer.parseInt(queryParameters.get("id").get(0));
        Observable<Product> items = catalogDao.getProducts();
        Observable<User> user = userDao.getUserById(id);

        return user.isEmpty().flatMap(
                isEmpty ->
                {
                    if (isEmpty) {
                        return Observable.just("No user with id: " + id);
                    } else {
                        Observable<Product> currencyItems = user.flatMap(u -> items.map(product -> product.convertCurrency(u.getCurrency())));
                        return currencyItems.collect(StringBuilder::new, (sb, x) -> sb.append(x).append("\n")).map(StringBuilder::toString);
                    }
                }
        );
    }

    private Observable<String> register(HttpServerRequest<ByteBuf> req) {
        Optional<String> error = checkRequestParameters(req, Arrays.asList("id", "name", "currency"));

        if (error.isPresent()) {
            return Observable.just(error.get());
        }

        Map<String, List<String>> queryParameters = req.getQueryParameters();
        String name = queryParameters.get("name").get(0);
        int id = Integer.parseInt(queryParameters.get("id").get(0));

        String currency = getCurrency(queryParameters);

        return userDao.registerUser(id, currency, name).map(Objects::toString).onErrorReturn(Throwable::getMessage);
    }

    private Observable<String> addProduct(HttpServerRequest<ByteBuf> req) {
        Optional<String> error = checkRequestParameters(req, Arrays.asList("name", "value", "currency"));

        if (error.isPresent()) {
            return Observable.just(error.get());
        }

        Map<String, List<String>> queryParameters = req.getQueryParameters();
        String name = queryParameters.get("name").get(0);
        int value = Integer.parseInt(queryParameters.get("value").get(0));

        String currency = getCurrency(queryParameters);

        return catalogDao.addProduct(name, value, currency).map(Objects::toString).onErrorReturn(Throwable::getMessage);
    }

    private Optional<String> checkRequestParameters(HttpServerRequest<ByteBuf> req, List<String> parameters) {
        String noParametersError = parameters.stream().filter(x -> !req.getQueryParameters().containsKey(x)).collect(Collectors.joining(", "));
        return (noParametersError.isEmpty()) ? Optional.empty() : Optional.of("no parameters in request: " + noParametersError);
    }

    private String getCurrency(Map<String, List<String>> queryParameters) {
        return queryParameters.get("currency").get(0).toUpperCase();
    }

}
