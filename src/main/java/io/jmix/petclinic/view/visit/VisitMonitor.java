package io.jmix.petclinic.view.visit;

import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataLoadContext;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.visit.Visit;
import io.jmix.petclinic.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Subscribe
    public void onReady(final ReadyEvent event) {
        // todo load items here
    }
    
    

    private BigDecimal randomPrice() {
        long l = ThreadLocalRandom.current().nextLong(1000, 10000);
        return BigDecimal.valueOf(l).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }
/*
    @Supply(to = "visitsDataGrid.lastUpdateDate", subject = "renderer")
    private Renderer<VisitInfo> visitsDataGridLastUpdateDateRenderer() {
        return new TextRenderer<>(visit -> visit.getLastUpdated().format(timeFormatter));
    }*/

    // @Subscribe("timer")
    public void onTimerTimerAction(final Timer.TimerActionEvent event) {
        Set<Integer> numbersToUpdate = new HashSet<>();
        int itemCount = visitInfoDc.getItems().size();
        for (int i = 0; i < 3; i++) {
            numbersToUpdate.add(ThreadLocalRandom.current().nextInt(0, itemCount));
        }
        for (int number : numbersToUpdate) {
            VisitInfo item = visitInfoDc.getItems().get(number);
            item.setPrice(randomPrice());
            item.setLastUpdated(LocalTime.now());
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
                    vi.setPrice(randomPrice());
                    vi.setLastUpdated(LocalTime.now());
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
}