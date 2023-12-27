package org.openmrs.module.ipd.api.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrugOrderSchedule {

    private List<Long> firstDaySlotsStartTime;
    private List<Long> dayWiseSlotsStartTime;
    private List<Long> remainingDaySlotsStartTime;
    private Long slotStartTime;
    private List<Slot> slots;

}
