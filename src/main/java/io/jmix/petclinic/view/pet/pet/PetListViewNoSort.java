package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.petclinic.view.main.MainView;

@Route(value = "pets", layout = MainView.class)
@ViewController("petclinic_Pet.list")
@ViewDescriptor("pet-list-view-no-sort.xml")
@LookupComponent("petsDataGrid")
@DialogMode(width = "50em")
public class PetListViewNoSort extends BasePetListView {

}
