package jebl01.krytex.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransientService {
    public final String name;
    public final String url;

    @JsonCreator
    public TransientService(@JsonProperty("name") final String name,
                            @JsonProperty("url") final String url) {
        this.name = name;
        this.url = url;
    }
}
