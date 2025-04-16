package io.jmix.petclinic.view.visit;

import org.springframework.context.ApplicationEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class VisitUpdatedEvent extends ApplicationEvent {

    private final Collection<UUID> updatedVisitIds;

    public VisitUpdatedEvent(Object source, Collection<UUID> updatedVisitIds) {
        super(source);
        this.updatedVisitIds = updatedVisitIds;
    }

    public Collection<UUID> getUpdatedVisitIds() {
        return Collections.unmodifiableCollection(updatedVisitIds);
    }
}
