package io.jmix.petclinic.view.pet.pet;

import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.petclinic.entity.NamedEntity;
import io.jmix.petclinic.entity.pet.Pet;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Loads and holds data necessary to display a custom column.
 * Also used for in-memory sorting.
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component("petclinic_LastVisitDateCache")
public class LastVisitDateCache {

    private final DataManager dataManager;

    private Map<UUID, LocalDateTime> lastVisits = new HashMap<>();

    public LastVisitDateCache(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public LocalDateTime getLastVisitDate(Pet pet) {
        return lastVisits.get(pet.getId());
    }

    public void loadLastVisits(List<Pet> petList) {
        lastVisits.clear();
        List<UUID> petIds = petList
                .stream()
                .map(NamedEntity::getId)
                .toList();

        // here we are lucky - we can load all necessary data just with one JPQL query
        // however: it doesn't consider possible row-level constraints
        List<KeyValueEntity> data = dataManager.loadValues(
                        "select p.id, max(v.visitStart)" +
                                " from petclinic_Visit v join v.pet p" +
                                " where p.id in :petIds" +
                                " group by p.id")
                .properties("pet", "maxVisitStart")
                .parameter("petIds", petIds)
                .list();
        for (KeyValueEntity item : data) {
            lastVisits.put(item.getValue("pet"), item.getValue("maxVisitStart"));
        }
    }

}