package io.jmix.petclinic.view.visit;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.visit.Visit;
import io.jmix.petclinic.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static java.time.temporal.ChronoField.*;


@Route(value = "visit-monitor", layout = MainView.class)
@ViewController(id = "petclinic_Visit.monitor")
@ViewDescriptor(path = "visit-monitor.xml")
@LookupComponent("visitsDataGrid")
@DialogMode(width = "64em")
public class VisitMonitor extends StandardListView<Visit> {

    @Autowired
    private CurrentAuthentication currentAuthentication;

    private DateTimeFormatter timeFormatter;
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private CollectionContainer<VisitInfo> visitInfoDc;
    @ViewComponent
    private CollectionContainer<Visit> visitsDc;
    @Autowired
    private Metadata metadata;
    @ViewComponent
    private DataContext dataContext;
    @Autowired
    private TimeSource timeSource;

    private int priceCounter = 10;

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

    @Subscribe("timer")
    public void onTimerTimerAction(final Timer.TimerActionEvent event) {
        priceCounter++;

        Set<Integer> numbersToUpdate = new HashSet<>();
        int itemCount = visitInfoDc.getItems().size();
        for (int i = 0; i < 5; i++) {
            numbersToUpdate.add(ThreadLocalRandom.current().nextInt(0, itemCount));
        }
        for (int number : numbersToUpdate) {
            VisitInfo item = visitInfoDc.getItems().get(number);
            item.setPrice(BigDecimal.valueOf(priceCounter));
            item.setLastUpdated(LocalDateTime.now());
        }
    }

    @Install(to = "visitInfoDl", target = Target.DATA_LOADER)
    private List<VisitInfo> visitInfoDlLoadDelegate(final LoadContext<VisitInfo> loadContext) {
        List<Visit> visits = dataManager.load(Visit.class)
                .query("select v from petclinic_Visit v")
                .fetchPlan(visitsDc.getFetchPlan())
                .firstResult(loadContext.getQuery().getFirstResult())
                .maxResults(loadContext.getQuery().getMaxResults())
                .list();

        return visits.stream()
                .map(v -> {
                    VisitInfo vi = dataContext.create(VisitInfo.class);
                    vi.setId(v.getId());
                    vi.setVisit(v);
                    vi.setPrice(BigDecimal.valueOf(priceCounter));
                    return vi;
                })
                .toList();
    }

    @Install(to = "pagination", subject = "totalCountDelegate")
    private Integer paginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        LoadContext<Visit> lc = new LoadContext<>(metadata.getClass(Visit.class));
        lc.setQueryString("select v from petclinic_Visit v");

        return (int) dataManager.getCount(lc);
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