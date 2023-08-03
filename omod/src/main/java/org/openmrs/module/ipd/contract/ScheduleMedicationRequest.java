package org.openmrs.module.ipd.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMedicationRequest {

    private String patientUuid;
    private String orderUuid;
    private String providerUuid;
    private String comments;
    private LocalDateTime slotStartTime;
    private List<LocalDateTime> firstDaySlotsStartTime;
    private List<LocalDateTime> dayWiseSlotsStartTime;
    private MedicationFrequency medicationFrequency;

    public enum MedicationFrequency {
        START_TIME_DURATION_FREQUENCY,
        FIXED_SCHEDULE_FREQUENCY
    }

    public LocalDateTime getSlotStartTimeAsLocaltime() {
        return slotStartTime != null ? convertUTCToLocalTimeZone(slotStartTime): null;
    }

    public List<LocalDateTime> getFirstDaySlotsStartTimeAsLocalTime() {
        return firstDaySlotsStartTime != null ? firstDaySlotsStartTime.stream().map(this::convertUTCToLocalTimeZone).collect(Collectors.toList()) : null;
    }

    public List<LocalDateTime> getDayWiseSlotsStartTimeAsLocalTime() {
        return dayWiseSlotsStartTime != null ? dayWiseSlotsStartTime.stream().map(this::convertUTCToLocalTimeZone).collect(Collectors.toList()) : null;
    }

    private LocalDateTime convertUTCToLocalTimeZone(LocalDateTime slotStartTime) {
        return slotStartTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }
}