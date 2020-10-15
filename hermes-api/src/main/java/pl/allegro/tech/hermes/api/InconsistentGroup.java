package pl.allegro.tech.hermes.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class InconsistentGroup {
    private final String name;
    private final List<EntityCopy> copies;
    private final List<InconsistentTopic> inconsistentTopics;

    @JsonCreator
    public InconsistentGroup(@JsonProperty("name") String name,
                             @JsonProperty("contents") List<EntityCopy> copies,
                             @JsonProperty("inconsistentTopics") List<InconsistentTopic> inconsistentTopics) {
        this.name = name;
        this.copies = copies;
        this.inconsistentTopics = inconsistentTopics;
    }

    public String getName() {
        return name;
    }

    public List<EntityCopy> getCopies() {
        return copies;
    }

    public List<InconsistentTopic> getInconsistentTopics() {
        return inconsistentTopics;
    }
}
