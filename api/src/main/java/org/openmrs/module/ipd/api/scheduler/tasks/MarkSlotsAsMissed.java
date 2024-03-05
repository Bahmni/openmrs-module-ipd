package org.openmrs.module.ipd.api.scheduler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MarkSlotsAsMissed extends AbstractTask {
    @Override
    public void execute() {

        SlotService slotService = Context.getService(SlotService.class);
        LocalDateTime currentDateTime = LocalDateTime.now();
        List<Slot> lastSlotForAnOrder = slotService.getLastSlotForAnOrder(currentDateTime);
        List<Order> orders = new ArrayList<>();
        Map<Order, LocalDateTime> maxTimeForAnOrder = new HashMap<>();
        if (!lastSlotForAnOrder.isEmpty()) {
            lastSlotForAnOrder.stream().forEach(slot -> {
                maxTimeForAnOrder.put(slot.getOrder(), slot.getStartDateTime());
                orders.add(slot.getOrder());
            });
            List<Slot> scheduledSlots = slotService.getScheduledSlots(orders);
            slotService.markSlotsAsMissed(scheduledSlots,  maxTimeForAnOrder);
        }
    }

}
