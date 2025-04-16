package io.jmix.petclinic.view.visit;

import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.visit.Visit;
import io.jmix.petclinic.view.main.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoField.*;


@Route(value = "visit-monitor", layout = MainView.class)
@ViewController(id = "petclinic_Visit.monitor")
@ViewDescriptor(path = "visit-monitor.xml")
@LookupComponent("visitsDataGrid")
@DialogMode(width = "64em")
public class VisitMonitor extends StandardListView<Visit> {
    private static final Logger log = LoggerFactory.getLogger(VisitMonitor.class);

    @Autowired
    private CurrentAuthentication currentAuthentication;
    @ViewComponent
    private CollectionContainer<VisitInfo> visitInfoDc;
    @Autowired
    private TimeSource timeSource;
    @Autowired
    private VisitCache visitCache;

    private DateTimeFormatter timeFormatter;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DatatypeRegistry datatypeRegistry;

    @Subscribe
    public void onInit(final InitEvent event) {
        timeFormatter = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .toFormatter(currentAuthentication.getLocale());
    }

    @Supply(to = "visitsDataGrid.lastUpdated", subject = "renderer")
    private Renderer<VisitInfo> visitsDataGridLastUpdatedRenderer() {
        return new TextRenderer<>(visit -> {
            return visit.getLastUpdated() != null
                    ? visit.getLastUpdated().format(timeFormatter)
                    : null;
        });
    }

    @Supply(to = "visitsDataGrid.price", subject = "renderer")
    private Renderer<VisitInfo> visitsDataGridPriceRenderer() {
        return new ComponentRenderer<>(visitInfo -> {
            TypedTextField<BigDecimal> textField = uiComponents.create(TypedTextField.class);
            textField.setDatatype(datatypeRegistry.get(BigDecimal.class));
            textField.setTypedValue(visitInfo.getPrice());
            textField.addTypedValueChangeListener(event -> {
                BigDecimal newPrice = event.getValue();
                visitInfo.setPrice(newPrice);
                visitCache.setPrice(visitInfo.getId(), newPrice);
                log.info("Price changed to {}", newPrice);
            });
            textField.setWidth("5em");
            return textField;
        });
    }

    @Subscribe("timer")
    public void onTimerTimerAction(final Timer.TimerActionEvent event) {
        visitInfoDc.mute();
        try {
            List<VisitInfo> visitItems = visitInfoDc.getMutableItems();
            visitItems.clear();
            visitItems.addAll(visitCache.getVisits());
        } finally {
            visitInfoDc.unmute(CollectionContainer.UnmuteEventsMode.FIRE_REFRESH_EVENT);
        }
        log.info("Full update done");
    }

    @EventListener
    public void visitsChanged(VisitUpdatedEvent event) {
        for (UUID visitId : event.getUpdatedVisitIds()) {
            VisitInfo visit = visitCache.getItemById(visitId);
            // provokes repaint for all rows
            visitInfoDc.replaceItem(visit);

            // copy in place all attributes that could be changed
            // VisitInfo containerItem = visitInfoDc.getItem(visitId);
            // containerItem.setPrice(visit.getPrice());
            // containerItem.setLastUpdated(visit.getLastUpdated());
        }
        log.info("UI updated by event");
    }

    @Install(to = "visitInfoDl", target = Target.DATA_LOADER)
    private List<VisitInfo> visitInfoDlLoadDelegate(final LoadContext<VisitInfo> loadContext) {
        return visitCache.getVisits();
    }

    @Install(to = "visitsDataGrid.lastUpdated", subject = "partNameGenerator")
    private String visitsDataGridLastUpdatedPartNameGenerator(final VisitInfo visitInfo) {
        LocalDateTime borderTime = timeSource.now().toLocalDateTime().minusSeconds(30);
        if (visitInfo.getLastUpdated() != null && visitInfo.getLastUpdated().isAfter(borderTime)) {
            return "rec-upd"; // recently updated
        }
        return null;
    }
}