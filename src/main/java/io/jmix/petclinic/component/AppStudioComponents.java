package io.jmix.petclinic.component;

import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface AppStudioComponents {
    @StudioComponent(
            name = "Slider",
            classFqn = "io.jmix.petclinic.component.Slider",
            category = "Components",
            xmlElement = "slider",
            xmlns = "http://company.com/schema/app-ui-components",
            xmlnsAlias = "app",
            properties = {
                    /* Common attributes */
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "alignSelf", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),

                    /* Size attributes */
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),

                    /* Slider attributes */
                    @StudioProperty(xmlAttribute = "min", type = StudioPropertyType.INTEGER,
                            defaultValue = "0"),
                    @StudioProperty(xmlAttribute = "max", type = StudioPropertyType.INTEGER,
                            defaultValue = "100")
            }
    )
    void slider();
}
