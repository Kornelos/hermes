package pl.allegro.tech.hermes.management.domain.consistency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.stereotype.Component;
import pl.allegro.tech.hermes.api.EntityCopy;
import pl.allegro.tech.hermes.api.Group;
import pl.allegro.tech.hermes.api.InconsistentGroup;
import pl.allegro.tech.hermes.api.InconsistentSubscription;
import pl.allegro.tech.hermes.api.InconsistentTopic;
import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.api.Topic;
import pl.allegro.tech.hermes.api.TopicName;
import pl.allegro.tech.hermes.domain.group.GroupRepository;
import pl.allegro.tech.hermes.domain.subscription.SubscriptionRepository;
import pl.allegro.tech.hermes.domain.topic.TopicRepository;
import pl.allegro.tech.hermes.management.domain.dc.DatacenterBoundRepositoryHolder;
import pl.allegro.tech.hermes.management.domain.dc.RepositoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class ConsistencyService {
    private final ExecutorService executor;
    private final List<DatacenterBoundRepositoryHolder<GroupRepository>> groupRepository;
    private final List<DatacenterBoundRepositoryHolder<TopicRepository>> topicRepository;
    private final List<DatacenterBoundRepositoryHolder<SubscriptionRepository>> subscriptionRepository;
    private final ObjectMapper objectMapper;

    public ConsistencyService(RepositoryManager repositoryManager, ObjectMapper objectMapper) {
        this.groupRepository = repositoryManager.getRepositories(GroupRepository.class);
        this.topicRepository = repositoryManager.getRepositories(TopicRepository.class);
        this.subscriptionRepository = repositoryManager.getRepositories(SubscriptionRepository.class);
        this.objectMapper = objectMapper;
        this.executor = Executors.newFixedThreadPool(
                10,
                new ThreadFactoryBuilder()
                        .setNameFormat("consistency-check-%d")
                        .build()
        );
    }

    public List<InconsistentGroup> findInconsistentGroups() {
        List<InconsistentGroup> inconsistentGroups = new ArrayList<>();
        for (EntityCopies copies : listCopiesOfAllGroups()) {
            List<InconsistentTopic> inconsistentTopics = findInconsistentTopics(copies.getEntityId());
            if (!copies.areAllEqual() || !inconsistentTopics.isEmpty()) {
                inconsistentGroups.add(new InconsistentGroup(copies.getEntityId(), toEntityCopy(copies), inconsistentTopics));
            }
        }
        return inconsistentGroups;
    }

    private List<EntityCopies> listCopiesOfAllGroups() {
        Map<String, Future<List<Group>>> results = new HashMap<>();
        for (DatacenterBoundRepositoryHolder<GroupRepository> repositoryHolder : groupRepository) {
            Future<List<Group>> submit = executor.submit(() -> repositoryHolder.getRepository().listGroups());
            results.put(repositoryHolder.getDatacenterName(), submit);
        }
        return findAll(results, Group::getGroupName);
    }

    private List<InconsistentTopic> findInconsistentTopics(String group) {
        List<InconsistentTopic> inconsistentTopics = new ArrayList<>();
        for (EntityCopies copies : listCopiesOfTopicsFromGroup(group)) {
            List<InconsistentSubscription> inconsistentSubscriptions = findInconsistentSubscriptions(copies.getEntityId());
            if (!copies.areAllEqual() || !inconsistentSubscriptions.isEmpty()) {
                inconsistentTopics.add(new InconsistentTopic(copies.getEntityId(), toEntityCopy(copies), inconsistentSubscriptions));
            }
        }
        return inconsistentTopics;
    }

    private List<EntityCopies> listCopiesOfTopicsFromGroup(String group) {
        Map<String, Future<List<Topic>>> results = new HashMap<>();
        for (DatacenterBoundRepositoryHolder<TopicRepository> repositoryHolder : topicRepository) {
            Future<List<Topic>> submit = executor.submit(() -> repositoryHolder.getRepository().listTopics(group));
            results.put(repositoryHolder.getDatacenterName(), submit);
        }
        return findAll(results, Topic::getQualifiedName);
    }

    private List<InconsistentSubscription> findInconsistentSubscriptions(String topic) {
        return listCopiesOfSubscriptionsFromTopic(topic).stream()
                .filter(copies -> !copies.areAllEqual())
                .map(copies -> new InconsistentSubscription(copies.getEntityId(), toEntityCopy(copies)))
                .collect(toList());
    }

    private List<EntityCopies> listCopiesOfSubscriptionsFromTopic(String topic) {
        Map<String, Future<List<Subscription>>> results = new HashMap<>();
        for (DatacenterBoundRepositoryHolder<SubscriptionRepository> repositoryHolder : subscriptionRepository) {
            Future<List<Subscription>> submit = executor.submit(
                    () -> repositoryHolder.getRepository().listSubscriptions(TopicName.fromQualifiedName(topic))
            );
            results.put(repositoryHolder.getDatacenterName(), submit);
        }
        return findAll(results, subscription -> subscription.getQualifiedName().getQualifiedName());
    }

    private <T> List<EntityCopies> findAll(Map<String, Future<List<T>>> futures, Function<T, String> idFunc) {
        Map<String, EntityCopies> result = new HashMap<>();
        for (Map.Entry<String, Future<List<T>>> entry : futures.entrySet()) {
            List<T> groups;
            try {
                groups = entry.getValue().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String datacenter = entry.getKey();
            for (T group : groups) {
                String id = idFunc.apply(group);
                EntityCopies copies = result.getOrDefault(id, new EntityCopies(id));
                copies.put(datacenter, group);
                result.put(id, copies);
            }
        }
        return new ArrayList<>(result.values());
    }

    private List<EntityCopy> toEntityCopy(EntityCopies copies) {
        return copies.getCopyPerDatacenter().entrySet().stream()
                .map(entry -> {
                    try {
                        return new EntityCopy(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
