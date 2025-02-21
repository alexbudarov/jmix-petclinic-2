package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.petclinic.entity.NamedEntity;
import io.jmix.petclinic.entity.pet.Pet;

import io.jmix.petclinic.entity.visit.Visit;
import io.jmix.petclinic.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Route(value = "pets", layout = MainView.class)
@ViewController("petclinic_Pet.list")
@ViewDescriptor("pet-list-view.xml")
@LookupComponent("petsDataGrid")
@DialogMode(width = "50em")
public class PetListView extends StandardListView<Pet> {

    @ViewComponent
    private PropertyFilter identificationNumberFilter;
    @ViewComponent
    private PropertyFilter typeFilter;
    @ViewComponent
    private PropertyFilter ownerFilter;
    @Autowired
    private DatatypeFormatter datatypeFormatter;

    // last visits cache
    private Map<Pet, Visit> lastVisits = new HashMap<>();

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
            Visit lastVisit = lastVisits.get(pet);
            return lastVisit != null
                    ? datatypeFormatter.formatLocalDateTime(lastVisit.getVisitStart())
                    : "";
        });
    }

    @Subscribe(id = "petsDl", target = Target.DATA_LOADER)
    public void onPetsDlPostLoad(final CollectionLoader.PostLoadEvent<Pet> event) {
        List<Pet> petList = event.getLoadedEntities();

        reloadLastVisits(petList);
    }

    private void reloadLastVisits(List<Pet> petList) {
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
            Visit visitFromMap = lastVisits.get(pet);
            if (visitFromMap == null) {
                lastVisits.put(pet, visit);
            } else if (visit.getVisitStart().isAfter(visitFromMap.getVisitStart())) {
                lastVisits.put(pet, visit);
            }
        }
    }
}
