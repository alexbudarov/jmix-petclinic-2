package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.pet.Pet;
import io.jmix.petclinic.view.main.MainView;

import java.util.List;

@Route(value = "pets", layout = MainView.class)
@ViewController("petclinic_Pet.list")
@ViewDescriptor("pet-list-view-no-sort.xml")
@LookupComponent("petsDataGrid")
@DialogMode(width = "50em")
public class PetListViewNoSort extends BasePetListView {

    @Subscribe(id = "petsDl", target = Target.DATA_LOADER)
    public void onPetsDlPostLoad(final CollectionLoader.PostLoadEvent<Pet> event) {
        List<Pet> petList = event.getLoadedEntities();

        lastVisitDateCache.loadLastVisits(petList);
    }
}
