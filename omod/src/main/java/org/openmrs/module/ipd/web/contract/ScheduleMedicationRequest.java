/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertEpocUTCToLocalTimeZone;

@Getter
@Setter
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
    private List<Long> remainingDaySlotsStartTime;
    private MedicationFrequency medicationFrequency;
    private ServiceType serviceType;

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

    public List<LocalDateTime> getRemainingDaySlotsStartTimeAsLocalTime() {
        return remainingDaySlotsStartTime != null ? remainingDaySlotsStartTime.stream().map(DateTimeUtil::convertEpocUTCToLocalTimeZone).collect(Collectors.toList()) : null;
    }
}
