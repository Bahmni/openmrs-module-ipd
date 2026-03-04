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
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;

import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertLocalDateTimeToUTCEpoc;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationScheduleResponse {

    private Integer id;
    private String uuid;
    private String serviceType;
    private String comments;
    private long startDate;
    private Object endDate;
    private List<MedicationSlotResponse> slots;
    public static MedicationScheduleResponse createFrom(Schedule schedule, List<Slot> slots) {

        return MedicationScheduleResponse.builder()
                .id(schedule.getId())
                .uuid(schedule.getUuid())
                .serviceType(schedule.getServiceType().getName().getName())
                .comments(schedule.getComments())
                .startDate(convertLocalDateTimeToUTCEpoc(schedule.getStartDate()))
                .endDate(schedule.getEndDate() != null ? convertLocalDateTimeToUTCEpoc(schedule.getEndDate()) : null)
                .slots(slots.stream().map(MedicationSlotResponse::createFrom).collect(Collectors.toList()))
                .build();

    }

}

