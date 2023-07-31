package org.openmrs.module.ipd.service.impl;

import org.openmrs.DrugOrder;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.contract.ScheduleMedicationRequest.MedicationFrequency.FIXED_SCHEDULE_FREQUENCY;
import static org.openmrs.module.ipd.contract.ScheduleMedicationRequest.MedicationFrequency.START_TIME_DURATION_FREQUENCY;

@Component
public class SlotTimeCreationService {
    public List<LocalDateTime> createSlotsStartTimeFrom(ScheduleMedicationRequest scheduleMedicationRequest, Schedule savedSchedule) {
        DrugOrder order = (DrugOrder) savedSchedule.getOrder();
        int numberOfSlotsStartTimeToBeCreated = (int) (Math.ceil(order.getQuantity() / order.getDose()));
        List<LocalDateTime> slotsStartTime = new ArrayList<>();

        if (scheduleMedicationRequest.getSlotStartTime() != null && scheduleMedicationRequest.getMedicationFrequency() == START_TIME_DURATION_FREQUENCY) {
            int slotDurationInHours = (int) (Math.floor(24 / order.getFrequency().getFrequencyPerDay()));
            LocalDateTime slotStartTime = scheduleMedicationRequest.getSlotStartTime();
            while (numberOfSlotsStartTimeToBeCreated-- > 0) {
                slotsStartTime.add(slotStartTime);
                slotStartTime = slotStartTime.plusHours(slotDurationInHours);
            }
        } else if (scheduleMedicationRequest.getMedicationFrequency() == FIXED_SCHEDULE_FREQUENCY) {
            if (scheduleMedicationRequest.getFirstDaySlotsStartTime() != null && !scheduleMedicationRequest.getFirstDaySlotsStartTime().isEmpty()) {
                if (numberOfSlotsStartTimeToBeCreated >= scheduleMedicationRequest.getFirstDaySlotsStartTime().size()) {
                    slotsStartTime.addAll(scheduleMedicationRequest.getFirstDaySlotsStartTime());
                    numberOfSlotsStartTimeToBeCreated -= scheduleMedicationRequest.getFirstDaySlotsStartTime().size();
                }
            }
            if (scheduleMedicationRequest.getDayWiseSlotsStartTime() != null && !scheduleMedicationRequest.getDayWiseSlotsStartTime().isEmpty()) {

                if (numberOfSlotsStartTimeToBeCreated >= scheduleMedicationRequest.getDayWiseSlotsStartTime().size()) {
                    slotsStartTime.addAll(scheduleMedicationRequest.getDayWiseSlotsStartTime());
                    numberOfSlotsStartTimeToBeCreated -= scheduleMedicationRequest.getDayWiseSlotsStartTime().size();
                }

                List<LocalDateTime> nextSlotsStartTime = scheduleMedicationRequest.getDayWiseSlotsStartTime();
                while (numberOfSlotsStartTimeToBeCreated > 0) {
                    nextSlotsStartTime = nextSlotsStartTime.stream().map(slotStartTime -> slotStartTime.plusHours(24)).collect(Collectors.toList());
                    if (numberOfSlotsStartTimeToBeCreated >= nextSlotsStartTime.size()) {
                        slotsStartTime.addAll(nextSlotsStartTime);
                        numberOfSlotsStartTimeToBeCreated -= nextSlotsStartTime.size();
                    } else {
                        slotsStartTime.addAll(nextSlotsStartTime.subList(0, numberOfSlotsStartTimeToBeCreated));
                        numberOfSlotsStartTimeToBeCreated -= nextSlotsStartTime.subList(0, numberOfSlotsStartTimeToBeCreated).size();
                    }
                }
            }
        }
        return slotsStartTime;
    }
}
