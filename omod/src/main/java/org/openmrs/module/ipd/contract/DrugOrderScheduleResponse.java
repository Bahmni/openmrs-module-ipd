package org.openmrs.module.ipd.contract;

import lombok.*;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.model.DrugOrderSchedule;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrugOrderScheduleResponse {

    private List<Long> firstDaySlotsStartTime;
    private List<Long> dayWiseSlotsStartTime;
    private List<Long> remainingDaySlotsStartTime;
    private Long slotStartTime;
    private Boolean medicationAdministrationStarted;
    private Boolean pendingSlotsAvailable;
    private String notes;

    public static DrugOrderScheduleResponse createFrom(DrugOrderSchedule drugOrderSchedule){
        return DrugOrderScheduleResponse.builder().
                firstDaySlotsStartTime(drugOrderSchedule.getFirstDaySlotsStartTime()).
                dayWiseSlotsStartTime(drugOrderSchedule.getDayWiseSlotsStartTime()).
                remainingDaySlotsStartTime(drugOrderSchedule.getRemainingDaySlotsStartTime()).
                slotStartTime(drugOrderSchedule.getSlotStartTime()).
                medicationAdministrationStarted(drugOrderSchedule.getSlots().stream().anyMatch(slot -> slot.getMedicationAdministration() != null)).
                pendingSlotsAvailable(drugOrderSchedule.getSlots().stream().anyMatch(slot -> slot.getStatus().equals(Slot.SlotStatus.SCHEDULED))).
                notes(drugOrderSchedule.getSlots().get(0).getNotes()).
                build();
    }
}
