package io.jmix.petclinic.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.sessions.SessionProfilerAdapter;
import org.springframework.lang.Nullable;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link SessionProfiler} that publishes some data to Micrometer registry.
 * Some code was copied from {@link org.eclipse.persistence.tools.profiler.PerformanceMonitor}.
 */
public class EclipseLinkPerformanceMonitor extends SessionProfilerAdapter {

    protected Map<Integer, Map<String, Long>> operationStartTimesByThread; // facilitates concurrency

    /**
     * Micrometer metrics binder
     */
    private EclipseLinkMetrics metrics;

    public EclipseLinkPerformanceMonitor() {
        this.operationStartTimesByThread = new ConcurrentHashMap<>();
    }

    @Override
    public void occurred(String operationName, AbstractSession session) {
        Counter counter = getCounter(operationName);
        if (counter != null) {
            counter.increment();
        }
    }

    @Nullable
    private Counter getCounter(String operationName) {
        if (metrics == null) {
            return null;
        }

        return switch (operationName) {
            case SessionProfiler.ClientSessionCreated -> metrics.getClientSessionsCreated();
            case SessionProfiler.ClientSessionReleased -> metrics.getClientSessionsReleased();
            case SessionProfiler.OptimisticLockException -> metrics.getOptimisticLockFailures();
            case SessionProfiler.RcmSent -> metrics.getRemoteMessagesSent();
            case SessionProfiler.RcmReceived -> metrics.getRemoteMessagesReceived();
            default -> null;
        };
    }

    protected Map<String, Long> getOperationStartTimes() {
        Integer threadId = Thread.currentThread().hashCode();
        Map<String, Long> times = this.operationStartTimesByThread.get(threadId);
        if (times == null) {
            times = new Hashtable<>();
            this.operationStartTimesByThread.put(threadId, times);
        }
        return times;
    }

    @Override
    public void startOperationProfile(String operationName) {
        if (getTimer(operationName) == null) {
            return;
        }
        getOperationStartTimes().put(operationName, System.nanoTime());
    }

    @Override
    public void startOperationProfile(String operationName, DatabaseQuery query, int weight) {
        startOperationProfile(operationName);
    }

    @Override
    public void endOperationProfile(String operationName) {
        Timer timer = getTimer(operationName);
        if (timer == null) {
            return;
        }

        long endTime = System.nanoTime();
        Long startTime = getOperationStartTimes().get(operationName);
        if (startTime == null) {
            return;
        }
        long time = endTime - startTime;
        timer.record(time, TimeUnit.NANOSECONDS);
    }

    @Nullable
    private Timer getTimer(String operationName) {
        if (metrics == null) {
            return null;
        }
        return switch (operationName) {
            case SessionProfiler.CacheCoordination -> metrics.getCacheCoordinationTimer();
            case SessionProfiler.StatementExecute -> metrics.getStatementExecuteTimer();
            case SessionProfiler.RowFetch -> metrics.getRowFetchTimer();
            default -> null;
        };
    }

    @Override
    public void endOperationProfile(String operationName, DatabaseQuery query, int weight) {
        endOperationProfile(operationName);
    }

    public void setMetrics(EclipseLinkMetrics metrics) {
        this.metrics = metrics;
    }
}
