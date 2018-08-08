package jebl01.krytex.model;

import io.vertx.core.Future;

import java.util.List;

public interface ServiceRepository {
    Future<List<Service>> getServices();

    Future<Void> persistAll(List<Service> services);

    Future<Service> persist(Service service);

    Future<Void> delete(String id);
}
