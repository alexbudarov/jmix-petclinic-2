package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Sort;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.pet.Pet;
import io.jmix.petclinic.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "pets/db-sort", layout = MainView.class)
@ViewController("petclinic_Pet_db_sort.list")
@ViewDescriptor("pet-list-view-db-paging-db-function-sort.xml")
@LookupComponent("petsDataGrid")
@DialogMode(width = "50em")
public class PetListViewDbPagingDbFunctionSort extends BasePetListView {
    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private CollectionLoader<Pet> petsDl;
    @ViewComponent
    private CollectionContainer<Pet> petsDc;
    @Autowired
    private QueryTransformerFactory queryTransformerFactory;

    private boolean loadedDataOnce = false;

    @Install(to = "petsDl", target = Target.DATA_LOADER)
    private List<Pet> petsDlLoadDelegate(final LoadContext<Pet> loadContext) {
        loadedDataOnce = true;

        if (isSortByLastVisitDate()) {
            // need to add a custom ORDER BY clause to the query string
            orderByJpqlFunction(loadContext);
        }

        List<Pet> pets = dataManager.loadList(loadContext);
        lastVisitDateCache.loadLastVisits(pets);
        return pets;
    }

    private void orderByJpqlFunction(LoadContext<Pet> loadContext) {
        // see io.jmix.data.impl.jpql.generator.SortJpqlGenerator for reference
        Map<String, Sort.Direction> sortExpressions = new HashMap<>();

        // use custom SQL function pet_last_visit_date(my_pet_id uuid)
        sortExpressions.put("function('pet_last_visit_date', {E}.id)",
                petsDataGrid.getSortOrder().getFirst().getDirection() == SortDirection.ASCENDING
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC
        );
        sortExpressions.put("{E}.id", Sort.Direction.ASC);

        QueryTransformer transformer = queryTransformerFactory.transformer(loadContext.getQuery().getQueryString());
        transformer.replaceOrderByExpressions(sortExpressions);
        String queryStringWithOrderByFunction = transformer.getResult();

        loadContext.getQuery().setQueryString(queryStringWithOrderByFunction);
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
