package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.pet.Pet;
import io.jmix.petclinic.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
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
    private CollectionLoader<Pet> petsDl;
    @ViewComponent
    private CollectionContainer<Pet> petsDc;

    private boolean loadedDataOnce = false;

    @Install(to = "petsDl", target = Target.DATA_LOADER)
    private List<Pet> petsDlLoadDelegate(final LoadContext<Pet> loadContext) {
        loadedDataOnce = true;

        if (isSortByLastVisitDate()) {
            // load full list, disregarding paging settings
            int firstResult = loadContext.getQuery().getFirstResult();
            int maxResults = loadContext.getQuery().getMaxResults();

            loadContext.getQuery().setFirstResult(0);
            loadContext.getQuery().setMaxResults(0);
            List<Pet> manyPets = dataManager.loadList(loadContext);

            lastVisitDateCache.loadLastVisits(manyPets);
            // sort full unpaged list
            sortByLastVisitDate(manyPets, petsDataGrid.getSortOrder().getFirst().getDirection());

            // extract one page
            int toIndex = Math.min(firstResult + maxResults, manyPets.size());
            List<Pet> page = manyPets.subList(firstResult, toIndex);
            return page;
        } else {
            // rely on database
            List<Pet> pets = dataManager.loadList(loadContext);
            lastVisitDateCache.loadLastVisits(pets);
            return pets;
        }
    }

    // sort in memory
    private void sortByLastVisitDate(List<Pet> pets, SortDirection direction) {
        Comparator<Pet> comparator = Comparator.comparing(pet -> {
            var date = lastVisitDateCache.getLastVisitDate(pet);
            return date != null ? date : LocalDateTime.MIN;
        });

        pets.sort(direction == SortDirection.ASCENDING ? comparator : comparator.reversed());
    }

    @Subscribe("petsDataGrid")
    public void onPetsDataGridSort(final SortEvent<DataGrid<Pet>, GridSortOrder<DataGrid<Pet>>> event) {
        // executed when user clicks column header's sort control
        // or when column sorting <settings/> are applied on screen opening

        // avoid excessive data loading when column sorting <settings/> are applied on screen opening
        if (!loadedDataOnce) {
            return;
        }

        if (isSortByLastVisitDate()
                // data grid tries to sort "in memory" under this condition
                // and actually does nothing, so we need to trigger data reloading
                && petsDl.getFirstResult() == 0
                && petsDc.getItems().size() < petsDl.getMaxResults()) {

            petsDl.load(); // petsDlLoadDelegate() will be called here.
        }
    }
}
