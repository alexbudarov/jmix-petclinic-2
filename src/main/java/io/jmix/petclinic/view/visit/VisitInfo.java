package io.jmix.petclinic.view.visit;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.petclinic.entity.visit.Visit;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@JmixEntity(name = "petclinic_VisitInfo")
public class VisitInfo {
    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private Visit visit;

    private BigDecimal price;

    private LocalTime lastUpdated;

    public void setLastUpdated(LocalTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalTime getLastUpdated() {
        return lastUpdated;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}