package org.openmrs.module.ipd.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertEpocUTCToLocalTimeZone;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMedicationRequest {

    private String patientUuid;
    private String orderUuid;
    private String providerUuid;
    private String comments;
    private Long slotStartTime;
    private List<Long> firstDaySlotsStartTime;
    private List<Long> dayWiseSlotsStartTime;
    private MedicationFrequency medicationFrequency;

    public enum MedicationFrequency {
        START_TIME_DURATION_FREQUENCY,
        FIXED_SCHEDULE_FREQUENCY
    }

    public LocalDateTime getSlotStartTimeAsLocaltime() {
        return slotStartTime != null ? convertEpocUTCToLocalTimeZone(slotStartTime): null;
    }

    public List<LocalDateTime> getFirstDaySlotsStartTimeAsLocalTime() {
        return firstDaySlotsStartTime != null ? firstDaySlotsStartTime.stream().map(DateTimeUtil::convertEpocUTCToLocalTimeZone).collect(Collectors.toList()) : null;
    }

    public List<LocalDateTime> getDayWiseSlotsStartTimeAsLocalTime() {
        return dayWiseSlotsStartTime != null ? dayWiseSlotsStartTime.stream().map(DateTimeUtil::convertEpocUTCToLocalTimeZone).collect(Collectors.toList()) : null;
    }
}