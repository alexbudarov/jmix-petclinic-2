package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.pet.Pet;
import io.jmix.petclinic.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "pets", layout = MainView.class)
@ViewController("petclinic_Pet.list")
@ViewDescriptor("pet-list-view-no-sort.xml")
@LookupComponent("petsDataGrid")
@DialogMode(width = "50em")
public class PetListViewNoSort extends BasePetListView {

    @Autowired
    private DataManager dataManager;

    @Install(to = "petsDl", target = Target.DATA_LOADER)
    private List<Pet> petsDlLoadDelegate(final LoadContext<Pet> loadContext) {
        return dataManager.loadList(loadContext);
    }

    @Subscribe(id = "petsDl", target = Target.DATA_LOADER)
    public void onPetsDlPostLoad(final CollectionLoader.PostLoadEvent<Pet> event) {
        List<Pet> petList = event.getLoadedEntities();

        reloadLastVisits(petList);
    }
}
