package jebl01.krytex.model;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Service {
    @JsonProperty("id")
    public final String id;
    @JsonProperty("name")
    public final String name;
    @JsonProperty("url")
    public final String url;
    @JsonProperty("status")
    public final Status status;
    @JsonProperty("lastCheck")
    public final Date lastCheck;

    @JsonCreator
    public Service(@JsonProperty("id") final String id,
                   @JsonProperty("name") final String name,
                   @JsonProperty("url") final String url,
                   @JsonProperty("status") final Status status,
                   @JsonProperty("lastCheck") final Date lastCheck) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.status = status;
        this.lastCheck = lastCheck;
    }

    public static Service createNew(final TransientService transientService) {
        return new Service(
            UUID.randomUUID().toString(),
            transientService.name,
            transientService.url,
            Status.UNKNOWN,
            null);
    }

    public Service cloneWithStatus(final Status status) {
        return new Service(
            this.id,
            this.name,
            this.url,
            status,
            status == Status.OK ? Date.from(Instant.now()) : this.lastCheck
        );
    }
}
