package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.pet.Pet;
import io.jmix.petclinic.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Route(value = "pets/nopaging", layout = MainView.class)
@ViewController("petclinic_Pet_nopaging.list")
@ViewDescriptor("pet-list-view-no-paging-memory-sort.xml")
@LookupComponent("petsDataGrid")
@DialogMode(width = "50em")
public class PetListViewNoPagingMemorySort extends BasePetListView {
    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private DataGrid<Pet> petsDataGrid;
    @ViewComponent
    private CollectionContainer<Pet> petsDc;

    @Install(to = "petsDl", target = Target.DATA_LOADER)
    private List<Pet> petsDlLoadDelegate(final LoadContext<Pet> loadContext) {
        // load full list (no pagination is configured)
        List<Pet> pets = dataManager.loadList(loadContext);
        reloadLastVisits(pets);

        // this will work when screen opens or after filtering
        if (isSortByLastVisitDate()) {
            sortByLastVisitDate(pets, petsDataGrid.getSortOrder().getFirst().getDirection());
        }
        return pets;
    }

    private boolean isSortByLastVisitDate() {
        return !petsDataGrid.getSortOrder().isEmpty()
                && "lastVisitDate".equals(petsDataGrid.getSortOrder().getFirst().getSorted().getKey());
    }

    // sort in memory
    private void sortByLastVisitDate(List<Pet> pets, SortDirection direction) {
        Comparator<Pet> comparator = Comparator.comparing(pet -> {
            return lastVisits.getOrDefault(pet, LocalDateTime.MIN);
        });

        pets.sort(direction == SortDirection.ASCENDING ? comparator : comparator.reversed());
    }

    @Subscribe("petsDataGrid")
    public void onPetsDataGridSort(final SortEvent<DataGrid<Pet>, GridSortOrder<DataGrid<Pet>>> event) {
        // this will work when user clicks column header's sort control
        if (isSortByLastVisitDate() && !petsDc.getItems().isEmpty()) {
            // sort in memory
            List<Pet> pets = new ArrayList<>(petsDc.getItems());
            var sortOrder = event.getSortOrder().getFirst();
            sortByLastVisitDate(pets, sortOrder.getDirection());
            petsDc.setItems(pets);
        }
    }
}
