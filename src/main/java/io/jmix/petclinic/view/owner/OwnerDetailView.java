package io.jmix.petclinic.view.owner;

import com.vaadin.componentfactory.addons.inputmask.InputMask;
import com.vaadin.componentfactory.addons.inputmask.InputMaskOption;
import com.vaadin.flow.component.html.H3;
import io.jmix.core.EntityStates;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.petclinic.entity.owner.Owner;

import io.jmix.petclinic.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "owners/:id", layout = MainView.class)
@ViewController("petclinic_Owner.detail")
@ViewDescriptor("owner-detail-view.xml")
@EditedEntityContainer("ownerDc")
public class OwnerDetailView extends StandardDetailView<Owner> {
    @ViewComponent
    private H3 nameHeader;
    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private TypedTextField<String> passportNumberField;
    @ViewComponent
    private TypedTextField<String> telephoneField;
    @Autowired
    private EntityStates entityStates;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        nameHeader.setText(messageBundle.formatMessage("ownerNameHeader", getEditedEntity().getFullName()));

        installPassportNumberInputMask();
        installPhoneNumberMask();
    }

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Owner> event) {
        event.getEntity().setTelephone("+7");
    }

    /*
     * Documentation for pattern formats:
     * https://imask.js.org/guide.html#masked-pattern
     */
    private void installPassportNumberInputMask() {
        // mask with restriction for russian letters
        InputMask mask = new InputMask(
                "bb 00 000000",
                InputMaskOption.option("definitions",
                        "{'b': /[А-Яа-я]/}",
                        true
                ),
                InputMaskOption.toUppercase()
        );
        mask.extend(passportNumberField);

        // this client-side restriction is incompatible with mask
        passportNumberField.setMaxLength(-1);

        passportNumberField.setHelperText("use format like: БЮ 01 123456");
    }

    private void installPhoneNumberMask() {
        InputMask mask = new InputMask(
                "+0(000)000-00-00",
                InputMaskOption.lazy(false),
                InputMaskOption.overwrite(true)
        );
        mask.extend(telephoneField);
    }
}
