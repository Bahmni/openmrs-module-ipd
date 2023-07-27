package org.openmrs.module.ipd.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMedicationRequest {

    private String patientUuid;
    private String orderUuid;
    private String providerUuid;
    private String comments;
    private Date slotStartTime;
    private List<Date> firstDaySlotsStartTime;
    private List<Date> dayWiseSlotsStartTime;
    private ScheduleStrategy scheduleStrategy;

    public enum ScheduleStrategy {
        SCHEDULE_BY_HOUR, SCHEDULE_BY_DAY
    }
}