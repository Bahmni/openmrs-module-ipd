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
import org.openmrs.module.ipd.web.model.PrescribedOrderSlotSummary;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescribedOrderSlotSummaryResponse {

    private String orderUuid;
    private List<MedicationSlotResponse> currentSlots;
    private MedicationSlotResponse previousSlot;
    private Long initialSlotStartTime;
    private Long finalSlotStartTime;

    public static PrescribedOrderSlotSummaryResponse createFrom(PrescribedOrderSlotSummary prescribedOrderSlotsSummary){
        MedicationSlotResponse previousSlot = prescribedOrderSlotsSummary.getPreviousSlot() != null
                ? MedicationSlotResponse.createFrom(prescribedOrderSlotsSummary.getPreviousSlot())
                : null;
        return PrescribedOrderSlotSummaryResponse.builder()
                .orderUuid(prescribedOrderSlotsSummary.getOrderUuid())
                .currentSlots(prescribedOrderSlotsSummary.getCurrentSlots().stream().map(slot -> MedicationSlotResponse.createFrom(slot)).collect(Collectors.toList()))
                .previousSlot(previousSlot)
                .initialSlotStartTime(prescribedOrderSlotsSummary.getInitialSlotStartTime())
                .finalSlotStartTime(prescribedOrderSlotsSummary.getFinalSlotStartTime())
                .build();
    }
}
