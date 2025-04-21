package io.jmix.petclinic.view.pet.pet;

import com.vaadin.flow.component.notification.Notification;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.petclinic.component.Slider;
import io.jmix.petclinic.entity.pet.Pet;

import io.jmix.petclinic.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "pets/:id", layout = MainView.class)
@ViewController("petclinic_Pet.detail")
@ViewDescriptor("pet-detail-view.xml")
@EditedEntityContainer("petDc")
@DialogMode
public class PetDetailView extends StandardDetailView<Pet> {

    @ViewComponent
    private JmixFormLayout form;
    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onInit(final InitEvent event) {
        Slider slider = new Slider();
        // slider.setWidth("20em");
        slider.setMin(10);
        slider.setMax(100);
        slider.getStyle().setMarginTop("1em");

        slider.addValueChangeListener(changedEvent -> {
            notifications.create("New value is : " + changedEvent.getValue())
                    .withPosition(Notification.Position.MIDDLE)
                    .show();
        });
        form.add(slider);
    }
}