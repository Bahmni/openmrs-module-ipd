package org.openmrs.module.ipd.service.impl;

import org.openmrs.DrugOrder;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.contract.ScheduleMedicationRequest.MedicationFrequency.FIXED_SCHEDULE_FREQUENCY;
import static org.openmrs.module.ipd.contract.ScheduleMedicationRequest.MedicationFrequency.START_TIME_DURATION_FREQUENCY;

@Component
public class SlotTimeCreationService {
    public List<LocalDateTime> createSlotsStartTimeFrom(ScheduleMedicationRequest request, Schedule savedSchedule) {
        DrugOrder order = (DrugOrder) savedSchedule.getOrder();

        if (request.getSlotStartTimeAsLocaltime() != null && request.getMedicationFrequency() == START_TIME_DURATION_FREQUENCY) {
            return getSlotsStartTimeWithStartTimeDurationFrequency(request, order);
        } else if (!CollectionUtils.isEmpty(request.getDayWiseSlotsStartTimeAsLocalTime()) && request.getMedicationFrequency() == FIXED_SCHEDULE_FREQUENCY) {
            return getSlotsStartTimeWithFixedScheduleFrequency(request, order);
        }

        return Collections.emptyList();
    }

    private List<LocalDateTime> getSlotsStartTimeWithFixedScheduleFrequency(ScheduleMedicationRequest request, DrugOrder order) {
        int numberOfSlotsStartTimeToBeCreated = (int) (Math.ceil(order.getQuantity() / order.getDose()));
        List<LocalDateTime> slotsStartTime = new ArrayList<>();

        if (!CollectionUtils.isEmpty(request.getFirstDaySlotsStartTimeAsLocalTime())) {
            List<LocalDateTime> slotsToBeAddedForFirstDay = numberOfSlotsStartTimeToBeCreated < request.getFirstDaySlotsStartTimeAsLocalTime().size()
                ? request.getFirstDaySlotsStartTimeAsLocalTime().subList(0, numberOfSlotsStartTimeToBeCreated)
                : request.getFirstDaySlotsStartTimeAsLocalTime();

            slotsStartTime.addAll(slotsToBeAddedForFirstDay);
            numberOfSlotsStartTimeToBeCreated -= slotsToBeAddedForFirstDay.size();
        }

        if (!CollectionUtils.isEmpty(request.getDayWiseSlotsStartTimeAsLocalTime()) && numberOfSlotsStartTimeToBeCreated > 0) {

            List<LocalDateTime> initialSlotsToBeAddedForSecondDay = numberOfSlotsStartTimeToBeCreated < request.getDayWiseSlotsStartTimeAsLocalTime().size()
                    ? request.getDayWiseSlotsStartTimeAsLocalTime().subList(0, numberOfSlotsStartTimeToBeCreated)
                    : request.getDayWiseSlotsStartTimeAsLocalTime();
            slotsStartTime.addAll(initialSlotsToBeAddedForSecondDay);
            numberOfSlotsStartTimeToBeCreated -= initialSlotsToBeAddedForSecondDay.size();

            List<LocalDateTime> nextSlotsStartTime = request.getDayWiseSlotsStartTimeAsLocalTime();
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

        return slotsStartTime;
    }

    private List<LocalDateTime> getSlotsStartTimeWithStartTimeDurationFrequency(ScheduleMedicationRequest request, DrugOrder order) {
        int numberOfSlotsStartTimeToBeCreated = (int) (Math.ceil(order.getQuantity() / order.getDose()));
        List<LocalDateTime> slotsStartTime = new ArrayList<>();
        int slotDurationInHours = (int) (Math.floor(24 / order.getFrequency().getFrequencyPerDay()));
        LocalDateTime slotStartTime = request.getSlotStartTimeAsLocaltime();
        while (numberOfSlotsStartTimeToBeCreated-- > 0) {
            slotsStartTime.add(slotStartTime);
            slotStartTime = slotStartTime.plusHours(slotDurationInHours);
        }
        return slotsStartTime;
    }
}