package io.jmix.petclinic.view.visit;

import io.jmix.core.*;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.petclinic.entity.visit.Visit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class VisitCache {
    private static final Logger log = LoggerFactory.getLogger(VisitCache.class);
    public static final int ITEMS_TO_UPDATE_PER_TICK = 2;

    private final FetchPlans fetchPlans;
    private final EntityStates entityStates;
    private final Copier copier;

    private LinkedHashMap<UUID, VisitInfo> visits = new LinkedHashMap<>();
    private volatile boolean initialized = false;

    private int priceCounter = 10;

    private final UnconstrainedDataManager unconstrainedDataManager;

    private final UiEventPublisher uiEventPublisher;

    public VisitCache(UnconstrainedDataManager unconstrainedDataManager, FetchPlans fetchPlans, EntityStates entityStates, Copier copier,
                      @Qualifier("flowui_UiEventPublisher") UiEventPublisher uiEventPublisher) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.fetchPlans = fetchPlans;
        this.entityStates = entityStates;
        this.copier = copier;
        this.uiEventPublisher = uiEventPublisher;
    }

    @EventListener
    public void handleApplicationReadyEvent(ApplicationReadyEvent event) {
        FetchPlan fetchPlan = fetchPlans.builder(Visit.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("assignedNurse", FetchPlan.INSTANCE_NAME)
                .add("pet", petBuilder -> {
                    petBuilder.addFetchPlan(FetchPlan.INSTANCE_NAME)
                            .add("type", FetchPlan.INSTANCE_NAME)
                            .add("owner", FetchPlan.INSTANCE_NAME);
                })
                .build();

        List<Visit> visitEntities = unconstrainedDataManager.load(Visit.class)
                .query("select v from petclinic_Visit v")
                .fetchPlan(fetchPlan)
                .maxResults(200)
                .list();

        visits = visitEntities.stream()
                .map(v -> {
                    VisitInfo vi = unconstrainedDataManager.create(VisitInfo.class);
                    vi.setId(v.getId());
                    vi.setVisit(v);
                    vi.setPrice(BigDecimal.valueOf(priceCounter));
                    entityStates.setNew(vi, false);
                    return vi;
                })
                .collect(Collectors.toMap(
                        VisitInfo::getId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        initialized = true;
    }

    /**
     * Creates a deep clone of objects stored in cache.
     */
    public List<VisitInfo> getVisits() {
        return copier.copy(visits.values().stream().toList());
    }

    /**
     * Creates a deep clone of stored object
     */
    public VisitInfo getItemById(UUID id) {
        VisitInfo value = visits.get(id);
        return value != null ? copier.copy(value) : null;
    }

    @Scheduled(fixedRate = 1000)
    public void updateCache() {
        if (!initialized) {
            return;
        }
        log.info("Updating cache {}", priceCounter);

        Set<UUID> idsToUpdate = new HashSet<>();
        int itemCount = visits.size();
        List<UUID> allIds = visits.keySet().stream().toList();
        for (int i = 0; i < ITEMS_TO_UPDATE_PER_TICK; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, itemCount);
            idsToUpdate.add(allIds.get(randomIndex));
        }

        for (UUID id : idsToUpdate) {
            VisitInfo item = visits.get(id);
            item.setPrice(BigDecimal.valueOf(priceCounter));
            item.setLastUpdated(LocalDateTime.now());
        }

        priceCounter++;
        VisitUpdatedEvent event = new VisitUpdatedEvent(this, idsToUpdate);
        uiEventPublisher.publishEventForUsers(event, null);
    }

    public void setPrice(UUID visitId, BigDecimal price) {
        Optional.of(visits.get(visitId))
                .ifPresent(v -> v.setPrice(price));
    }
}
