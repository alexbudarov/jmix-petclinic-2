package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.petclinic.entity.NamedEntity;
import io.jmix.petclinic.entity.pet.Pet;
import io.jmix.petclinic.entity.visit.Visit;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class BasePetListView extends StandardListView<Pet> {

    @ViewComponent
    private PropertyFilter identificationNumberFilter;
    @ViewComponent
    private PropertyFilter typeFilter;
    @ViewComponent
    private PropertyFilter ownerFilter;
    @Autowired
    private DatatypeFormatter datatypeFormatter;

    // last visit date cache
    protected Map<Pet, LocalDateTime> lastVisits = new HashMap<>();

    @Autowired
    private DataManager dataManager;

    @Subscribe("clearFilterAction")
    public void onClearFilterAction(final ActionPerformedEvent event) {
        identificationNumberFilter.clear();
        typeFilter.clear();
        ownerFilter.clear();
    }

    @Supply(to = "petsDataGrid.lastVisitDate", subject = "renderer")
    private Renderer<Pet> petsDataGridLastVisitDateRenderer() {
        return new TextRenderer<>(pet -> {
            LocalDateTime lastVisit = lastVisits.get(pet);
            return lastVisit != null
                    ? datatypeFormatter.formatLocalDateTime(lastVisit)
                    : "";
        });
    }

    // Load local cache of last visits
    protected void reloadLastVisits(List<Pet> petList) {
        lastVisits.clear();
        List<UUID> petIds = petList
                .stream()
                .map(NamedEntity::getId)
                .toList();

        List<Visit> allVisitsByPets = dataManager.load(Visit.class)
                .query("select v from petclinic_Visit v where v.pet.id in :petIds")
                .parameter("petIds", petIds)
                .fetchPlan(fetchPlanBuilder
                        -> fetchPlanBuilder.addFetchPlan(FetchPlan.LOCAL)
                        .add("pet", FetchPlan.INSTANCE_NAME)
                )
                .list();

        for (Visit visit: allVisitsByPets) {
            Pet pet = visit.getPet();
            LocalDateTime dateFromMap = lastVisits.get(pet);
            if (dateFromMap == null) {
                lastVisits.put(pet, visit.getVisitStart());
            } else if (visit.getVisitStart().isAfter(dateFromMap)) {
                lastVisits.put(pet, visit.getVisitStart());
            }
        }
    }
}
