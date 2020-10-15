package pl.allegro.tech.hermes.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class InconsistentTopic {
    private final String name;
    private final List<EntityCopy> copies;
    private final List<InconsistentSubscription> inconsistentSubscriptions;

    @JsonCreator
    public InconsistentTopic(@JsonProperty("name") String name,
                             @JsonProperty("copies") List<EntityCopy> copies,
                             @JsonProperty("inconsistentSubscriptions") List<InconsistentSubscription> inconsistentSubscriptions) {
        this.name = name;
        this.copies = copies;
        this.inconsistentSubscriptions = inconsistentSubscriptions;
    }

    public String getName() {
        return name;
    }

    public List<EntityCopy> getCopies() {
        return copies;
    }

    public List<InconsistentSubscription> getInconsistentSubscriptions() {
        return inconsistentSubscriptions;
    }
}
