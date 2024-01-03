package org.openmrs.module.ipd.contract;

import lombok.*;
import org.openmrs.module.ipd.model.DrugOrderSchedule;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrugOrderScheduleResponse {

    private List<Long> firstDaySlotsStartTime;
    private List<Long> dayWiseSlotsStartTime;
    private List<Long> remainingDaySlotsStartTime;
    private Long slotStartTime;
    private List<MedicationSlotResponse> slots;

    public static DrugOrderScheduleResponse createFrom(DrugOrderSchedule drugOrderSchedule){
            return DrugOrderScheduleResponse.builder().
                    firstDaySlotsStartTime(drugOrderSchedule.getFirstDaySlotsStartTime()).
                    dayWiseSlotsStartTime(drugOrderSchedule.getDayWiseSlotsStartTime()).
                    remainingDaySlotsStartTime(drugOrderSchedule.getRemainingDaySlotsStartTime()).
                    slotStartTime(drugOrderSchedule.getSlotStartTime()).
                    slots(drugOrderSchedule.getSlots().stream().map(slot -> MedicationSlotResponse.createFrom(slot, Representation.REF)).collect(Collectors.toList())).
                    build();
    }
}
