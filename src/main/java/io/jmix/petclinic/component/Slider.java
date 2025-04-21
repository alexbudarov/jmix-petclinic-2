package io.jmix.petclinic.component;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

@Tag("demo-slider")
@NpmPackage(value = "jquery", version = "1.9.1")
@NpmPackage(value = "jquery-ui", version = "1.13.2")
@CssImport("jquery-ui/dist/themes/base/jquery-ui.css")
@JsModule("./src/component/slider/slider.js")
public class Slider extends Component implements HasSize {
    private static final String VALUE_PROPERTY = "value";
    private static final String MIN_PROPERTY = "min";
    private static final String MAX_PROPERTY = "max";

    public Slider() {
    }

    public int getMin() {
        return getElement().getProperty(MIN_PROPERTY, 0);
    }

    public void setMin(int min) {
        getElement().setProperty(MIN_PROPERTY, min);
    }

    public int getMax() {
        return getElement().getProperty(MAX_PROPERTY, 100);
    }

    public void setMax(int max) {
        getElement().setProperty(MAX_PROPERTY, max);
    }

    @Synchronize("custom-slide-changed")
    public int getValue() {
        return getElement().getProperty(VALUE_PROPERTY, 0);
    }

    public void setValue(int value) {
        getElement().setProperty(VALUE_PROPERTY, value);
    }

    public Registration addValueChangeListener(ComponentEventListener<SlideChangedEvent> listener) {
        return addListener(SlideChangedEvent.class, listener);
    }

    @DomEvent("custom-slide-changed")
    public static class SlideChangedEvent extends ComponentEvent<Slider> {

        protected int value;

        public SlideChangedEvent(Slider source, boolean fromClient,
                                 @EventData("event.detail.value") int value) {
            super(source, fromClient);
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
