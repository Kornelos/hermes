package pl.allegro.tech.hermes.management.domain.consistency;

import java.util.HashMap;
import java.util.Map;

class EntityCopies {
    private final String entityId;
    private final Map<String, Object> copyPerDatacenter = new HashMap<>();

    EntityCopies(String entityId) {
        this.entityId = entityId;
    }

    void put(String datacenter, Object value) {
        copyPerDatacenter.put(datacenter, value);
    }

    boolean areAllEqual() {
        return copyPerDatacenter.values().stream().distinct().count() == 1;
    }

    String getEntityId() {
        return entityId;
    }

    Map<String, Object> getCopyPerDatacenter() {
        return copyPerDatacenter;
    }
}
