package org.openmrs.module.ipd.api.scheduler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MarkSlotsAsMissed extends AbstractTask {
    @Override
    public void execute() {
        SlotService slotService = Context.getService(SlotService.class);
        ConceptService conceptService = Context.getConceptService();
        LocalDateTime currentDateTime = LocalDateTime.now();
        List<Slot> lastSlotForAnOrder = slotService.getLastSlotForAnOrder(currentDateTime);
        List<Order> orders = new ArrayList<>();
        Map<Order, LocalDateTime> maxTimeForAnOrder = new HashMap<>();

        if (!lastSlotForAnOrder.isEmpty()) {
            lastSlotForAnOrder.forEach(slot -> {
                if (slot.getOrder() != null) {
                    maxTimeForAnOrder.put(slot.getOrder(), slot.getStartDateTime());
                    if (!orders.contains(slot.getOrder())) {
                        orders.add(slot.getOrder());
                    }
                } else {
                    log.warn("Slot with ID {} has a null order. Skipping for MarkSlotsAsMissed.", slot.getId());
                }
            });

            if (!orders.isEmpty()) {
                List<Slot> scheduledSlots = slotService.getScheduledSlots(orders);
                Concept asNeededPlaceholderServiceType = conceptService.getConceptByName(ServiceType.AS_NEEDED_PLACEHOLDER.conceptName());

                List<Slot> slotsToProcessForMissedStatus = scheduledSlots.stream()
                        .filter(slot -> {
                            // If the AS_NEEDED_PLACEHOLDER concept is not found, log a warning and process all slots to maintain previous behavior.
                            // This situation should ideally be addressed by ensuring the concept exists.
                            if (asNeededPlaceholderServiceType == null) {
                                log.warn("AS_NEEDED_PLACEHOLDER concept ('{}') not found. All scheduled slots will be processed for missed status. This might lead to incorrect PRN slot statuses.", ServiceType.AS_NEEDED_PLACEHOLDER.conceptName());
                                return true;
                            }
                            // Only process slots that are NOT AS_NEEDED_PLACEHOLDER, or if their serviceType is null.
                            return slot.getServiceType() == null || !slot.getServiceType().equals(asNeededPlaceholderServiceType);
                        })
                        .collect(Collectors.toList());

                if (!slotsToProcessForMissedStatus.isEmpty()) {
                     slotService.markSlotsAsMissed(slotsToProcessForMissedStatus, maxTimeForAnOrder);
                }
            }
        }
    }
}
