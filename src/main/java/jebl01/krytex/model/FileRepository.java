package jebl01.krytex.model;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.Json;
import io.vertx.core.parsetools.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

public class FileRepository implements ServiceRepository {
    private final Vertx vertx;
    private final String file;

    public FileRepository(final Vertx vertx, final String file) {
        this.vertx = vertx;
        this.file = file;
    }

    @Override
    public Future<List<Service>> getServices() {
        final Future<List<Service>> servicesFuture = Future.future();
        vertx.fileSystem().open(file, new OpenOptions(), ar -> {
            if(ar.failed()) {
                servicesFuture.fail(ar.cause());
            }
            else {
                final AsyncFile asyncFile = ar.result();
                final JsonParser jsonParser = JsonParser.newParser(asyncFile);
                jsonParser.arrayValueMode()
                    .exceptionHandler(t -> {
                        asyncFile.close();
                        servicesFuture.fail(t);
                    })
                    .endHandler(v -> {
                        if(!servicesFuture.isComplete()) {
                            servicesFuture.complete(Collections.emptyList());
                        }
                        asyncFile.close();
                    })
                    .handler(event -> servicesFuture.complete(event.mapTo(new TypeReference<List<Service>>() {
                    })));
            }
        });
        return servicesFuture;
    }

    @Override
    public Future<Void> persistAll(final List<Service> services) {
        final Future<Void> future = Future.future();
        vertx.executeBlocking(f -> {
            vertx.fileSystem().writeFile(file, Json.encodeToBuffer(services), f.completer());
        }, future.completer());
        return future;
    }

    @Override
    public Future<Service> persist(final Service service) {
        return getServices().compose(services -> {
            final List<Service> mutatedList = new ArrayList<>(services);
            mutatedList.add(service);
            return persistAll(mutatedList);
        }).compose(v -> Future.succeededFuture(service));
    }

    @Override
    public Future<Void> delete(final String id) {
        return getServices().compose(services -> {
            final List<Service> mutatedList = services.stream()
                .filter(service -> !Objects.equals(service.id, id))
                .collect(Collectors.toList());
            return persistAll(mutatedList);
        });
    }
}
