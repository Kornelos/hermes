package pl.allegro.tech.hermes.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class InconsistentSubscription {
    private final String name;
    private final List<EntityCopy> copies;

    @JsonCreator
    public InconsistentSubscription(@JsonProperty("name") String name,
                                    @JsonProperty("contents") List<EntityCopy> copies) {
        this.name = name;
        this.copies = copies;
    }

    public String getName() {
        return name;
    }

    public List<EntityCopy> getCopies() {
        return copies;
    }
}
