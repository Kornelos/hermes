package pl.allegro.tech.hermes.management.api;

import org.springframework.stereotype.Component;
import pl.allegro.tech.hermes.api.InconsistentGroup;
import pl.allegro.tech.hermes.management.domain.consistency.ConsistencyService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Component
@Path("consistency")
public class ConsistencyEndpoint {
    private final ConsistencyService consistencyService;

    public ConsistencyEndpoint(ConsistencyService consistencyService) {
        this.consistencyService = consistencyService;
    }

    @GET
    @Produces({APPLICATION_JSON})
    @Path("/")
    public Response findInconsistentGroups() {
        List<InconsistentGroup> inconsistentGroups = consistencyService.findInconsistentGroups();
        return Response.ok()
                .entity(new GenericEntity<List<InconsistentGroup>>(inconsistentGroups){})
                .build();
    }
}
