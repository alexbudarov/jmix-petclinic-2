package io.jmix.petclinic.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import jakarta.persistence.EntityManagerFactory;
import org.eclipse.persistence.jpa.JpaEntityManagerFactory;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Storage of Micrometer objects for EclipseLink's stats gatherer - {@link EclipseLinkPerformanceMonitor}.
 */
public class EclipseLinkMetrics implements MeterBinder {

    private static final Logger log = LoggerFactory.getLogger(EclipseLinkMetrics.class);
    private Counter clientSessionsCreated;
    private Counter clientSessionsReleased;

    private Counter optimisticLockFailures;

    private Counter remoteMessagesSent;
    private Counter remoteMessagesReceived;

    private Timer statementExecuteTimer;
    private Timer cacheCoordinationTimer;
    private Timer rowFetchTimer;

    public EclipseLinkMetrics(EntityManagerFactory emf) {
        DatabaseSession databaseSession = emf.unwrap(JpaEntityManagerFactory.class).getDatabaseSession();
        SessionProfiler profiler = databaseSession.getProfiler();
        if (profiler instanceof EclipseLinkPerformanceMonitor monitor) {
            monitor.setMetrics(this);
            log.info("Publishing EclipseLink stats to Micrometer");
        }
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        clientSessionsCreated = Counter.builder("eclipselink.clientsession")
                .tag("op", "created")
                .baseUnit(BaseUnits.SESSIONS)
                .register(registry);
        clientSessionsReleased = Counter.builder("eclipselink.clientsession")
                .tag("op", "released")
                .baseUnit(BaseUnits.SESSIONS)
                .register(registry);

        optimisticLockFailures = Counter.builder("eclipselink.optimistic.failures")
                .baseUnit(BaseUnits.EVENTS)
                .register(registry);

        remoteMessagesSent = Counter.builder("eclipselink.remote.messages")
                .tag("op", "sent")
                .baseUnit(BaseUnits.MESSAGES)
                .register(registry);

        remoteMessagesReceived = Counter.builder("eclipselink.remote.messages")
                .tag("op", "received")
                .baseUnit(BaseUnits.MESSAGES)
                .register(registry);

        statementExecuteTimer = registry.timer("eclipselink.statement.execute");
        rowFetchTimer = registry.timer("eclipselink.row.fetch");
        cacheCoordinationTimer = registry.timer("eclipselink.cache.coordination");
    }

    Counter getClientSessionsCreated() {
        return clientSessionsCreated;
    }

    Counter getClientSessionsReleased() {
        return clientSessionsReleased;
    }

    Counter getOptimisticLockFailures() {
        return optimisticLockFailures;
    }

    Counter getRemoteMessagesSent() {
        return remoteMessagesSent;
    }

    Counter getRemoteMessagesReceived() {
        return remoteMessagesReceived;
    }

    Timer getStatementExecuteTimer() {
        return statementExecuteTimer;
    }

    Timer getRowFetchTimer() {
        return rowFetchTimer;
    }

    Timer getCacheCoordinationTimer() {
        return cacheCoordinationTimer;
    }
}
