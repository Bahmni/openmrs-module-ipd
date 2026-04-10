/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.model.Schedule;

import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertLocalDateTimeToUTCEpoc;

@Builder
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleMedicationResponse {
    private Integer id;
    private String patientUuid;
    private String comments;
    private long startDate;
    private Object endDate;
    private Object order;

    public static ScheduleMedicationResponse constructFrom(Schedule schedule) {
        return ScheduleMedicationResponse.builder()
                .id(schedule.getId())
                .patientUuid(schedule.getSubject().getUuid())
                .comments(schedule.getComments())
//          .order(ConversionUtil.convertToRepresentation(schedule.getOrder(), Representation.REF)) // TODO. Clarify why we need to use REF here with product team
                .startDate(convertLocalDateTimeToUTCEpoc(schedule.getStartDate()))
                .endDate(schedule.getEndDate() != null ? convertLocalDateTimeToUTCEpoc(schedule.getEndDate()) : null)
                .build();
    }
}
