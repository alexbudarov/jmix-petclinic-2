package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.NamedEntity;
import io.jmix.petclinic.entity.pet.Pet;
import io.jmix.petclinic.entity.visit.Visit;
import io.jmix.petclinic.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Route(value = "pets/memory-paging", layout = MainView.class)
@ViewController("petclinic_Pet_memory_paging.list")
@ViewDescriptor("pet-list-view-memory-paging-memory-sort.xml")
@LookupComponent("petsDataGrid")
@DialogMode(width = "50em")
public class PetListViewMemoryPagingMemorySort extends BasePetListView {
    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private DataGrid<Pet> petsDataGrid;
    @ViewComponent
    private CollectionLoader<Pet> petsDl;
    @ViewComponent
    private CollectionContainer<Pet> petsDc;

    private boolean loadedDataOnce = false;

    @Install(to = "petsDl", target = Target.DATA_LOADER)
    private List<Pet> petsDlLoadDelegate(final LoadContext<Pet> loadContext) {
        loadedDataOnce = true;
        // this will work when screen opens or after filtering
        if (isSortByLastVisitDate()) {
            // load full list, disregarding paging settings
            int firstResult = loadContext.getQuery().getFirstResult();
            int maxResults = loadContext.getQuery().getMaxResults();

            loadContext.getQuery().setFirstResult(0);
            loadContext.getQuery().setMaxResults(0);
            List<Pet> manyPets = dataManager.loadList(loadContext);

            loadLastVisitDatesForLargePetList(manyPets);
            // sort full unpaged list
            sortByLastVisitDate(manyPets, petsDataGrid.getSortOrder().getFirst().getDirection());

            // extract one page
            int toIndex = Math.min(firstResult + maxResults, manyPets.size());
            List<Pet> page = manyPets.subList(firstResult, toIndex);
            return page;
        } else {
            // rely on database
            List<Pet> pets = dataManager.loadList(loadContext);
            reloadLastVisits(pets);
            return pets;
        }
    }

    private void loadLastVisitDatesForLargePetList(List<Pet> manyPets) {
        lastVisits.clear();
        // bulk load maximum last visit dates
        if (!manyPets.isEmpty()) {
            List<KeyValueEntity> data = dataManager.loadValues(
                            "select p, max(v.visitStart)" +
                                    " from petclinic_Visit v join v.pet p" +
                                    " where p in :petIds" +
                                    " group by p")
                    .properties("pet", "maxVisitStart")
                    .parameter("petIds", manyPets.stream().map(NamedEntity::getId).toList())
                    .list();
            for (KeyValueEntity item : data) {
                lastVisits.put(item.getValue("pet"), item.getValue("maxVisitStart"));
            }
        }
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
        if (isSortByLastVisitDate()
                && loadedDataOnce
                // data grid tries to sort "in memory" under this condition
                // and actually does nothing, so we need to trigger data reloading
                && petsDl.getFirstResult() == 0
                && petsDc.getItems().size() < petsDl.getMaxResults()) {

            petsDl.load(); // petsDlLoadDelegate() will be called here.
        }
    }
}
