package io.jmix.petclinic.view.visit;

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.petclinic.entity.visit.Visit;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class VisitCache {
    private final FetchPlans fetchPlans;

    private List<VisitInfo> visits = new ArrayList<>();

    private int priceCounter = 10;

    private final UnconstrainedDataManager unconstrainedDataManager;

    public VisitCache(UnconstrainedDataManager unconstrainedDataManager, FetchPlans fetchPlans) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.fetchPlans = fetchPlans;
    }

    @EventListener
    public void handleApplicationReadyEvent(ApplicationReadyEvent event) {
        FetchPlan fetchPlan = fetchPlans.builder(Visit.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("assignedNurse", FetchPlan.INSTANCE_NAME)
                .add("pet", petBuilder -> {
                    petBuilder.add(FetchPlan.INSTANCE_NAME)
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
                    return vi;
                })
                .toList();
    }

    public List<VisitInfo> getVisits() {
        return Collections.unmodifiableList(visits);
    }
}
