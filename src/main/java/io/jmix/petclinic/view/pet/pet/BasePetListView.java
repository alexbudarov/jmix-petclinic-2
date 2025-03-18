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
    @Autowired
    protected LastVisitDateCache lastVisitDateCache;

    @Subscribe("clearFilterAction")
    public void onClearFilterAction(final ActionPerformedEvent event) {
        identificationNumberFilter.clear();
        typeFilter.clear();
        ownerFilter.clear();
    }

    @Supply(to = "petsDataGrid.lastVisitDate", subject = "renderer")
    private Renderer<Pet> petsDataGridLastVisitDateRenderer() {
        return new TextRenderer<>(pet -> {
            LocalDateTime lastVisit = lastVisitDateCache.getLastVisitDate(pet);
            return lastVisit != null
                    ? datatypeFormatter.formatLocalDateTime(lastVisit)
                    : "";
        });
    }
}
