package jebl01.krytex;

import io.vertx.rxjava.core.http.HttpClient;
import io.vertx.rxjava.core.http.HttpClientRequest;
import io.vertx.rxjava.core.http.HttpClientResponse;
import jebl01.krytex.model.ServiceRepository;
import jebl01.krytex.model.Status;
import rx.Emitter;
import rx.Observable;
import rx.schedulers.Schedulers;

public class ServicePoller {
    private final ServiceRepository repository;
    private final HttpClient client;
    private final int maxConcurrentPolls;

    public ServicePoller(final ServiceRepository repository, final HttpClient client, final int maxConcurrentPolls) {
        this.repository = repository;
        this.client = client;
        this.maxConcurrentPolls = maxConcurrentPolls;
    }

    public void poll() {
        System.out.println("polling services!");
        repository.getServices().setHandler(ar -> {
            if(ar.failed()) {
                System.out.println("failed to get services from file");
            }
            else {
                Observable
                    .from(ar.result())
                    .flatMap(
                        service -> {
                            System.out.println("polling: " + service.url);
                            return getAbs(client, service.url)
                                .subscribeOn(Schedulers.io())
                                .map(s -> service.cloneWithStatus(s.statusCode() / 100 == 2 ? Status.OK : Status.FAIL));
                        },
                        maxConcurrentPolls)
                    .toList()
                    .subscribe(
                        services -> {
                            System.out.println("persisting polling result!");
                            repository.persistAll(services);
                        },
                        error -> {
                            System.out.println("failed to poll services!");
                            error.printStackTrace();
                        },
                        () -> System.out.println("finished polling services!"));
            }
        });
    }

    //RxHelper does not expose a "getAbs" method...(also, does not use deprecated "Observable.create(...)")
    public static Observable<HttpClientResponse> getAbs(HttpClient client, String requestURI) {
        return Observable.create((subscriber) -> {
            HttpClientRequest req = client.getAbs(requestURI);
            Observable<HttpClientResponse> resp = req.toObservable();
            resp.subscribe(subscriber);
            req.end();
        }, Emitter.BackpressureMode.DROP);
    }
}
