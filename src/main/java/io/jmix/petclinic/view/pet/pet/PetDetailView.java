package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.component.Slider;
import io.jmix.petclinic.entity.pet.Pet;
import io.jmix.petclinic.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "pets/:id", layout = MainView.class)
@ViewController("petclinic_Pet.detail")
@ViewDescriptor("pet-detail-view.xml")
@EditedEntityContainer("petDc")
@DialogMode
public class PetDetailView extends StandardDetailView<Pet> {

    @Autowired
    private Notifications notifications;
    @ViewComponent
    private Slider slider;

    @Subscribe
    public void onInit(final InitEvent event) {
        slider.addValueChangeListener(changedEvent -> {
            notifications.create("New value is : " + changedEvent.getValue())
                    .withPosition(Notification.Position.MIDDLE)
                    .show();
        });
    }
}