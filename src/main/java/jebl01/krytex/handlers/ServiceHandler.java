package jebl01.krytex.handlers;

import io.vertx.ext.web.RoutingContext;
import jebl01.krytex.model.ServiceRepository;

public abstract class ServiceHandler implements io.vertx.core.Handler<RoutingContext> {
    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json; charset=utf-8";

    protected final ServiceRepository repository;

    public ServiceHandler(final ServiceRepository repository) {
        this.repository = repository;
    }
}
