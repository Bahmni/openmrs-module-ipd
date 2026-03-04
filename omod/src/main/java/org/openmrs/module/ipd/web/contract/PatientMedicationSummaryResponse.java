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
import org.openmrs.module.ipd.web.model.PatientMedicationSummary;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedicationSummaryResponse {

    private String patientUuid;
    private List<PrescribedOrderSlotSummaryResponse> prescribedOrderSlots;
    private List<MedicationSlotResponse> emergencyMedicationSlots;

    public static PatientMedicationSummaryResponse createFrom(PatientMedicationSummary patientMedicationSummary) {
        List<PrescribedOrderSlotSummaryResponse> prescribedOrderSlots = patientMedicationSummary.getPrescribedOrderSlots() != null
                ? patientMedicationSummary.getPrescribedOrderSlots().stream().map(PrescribedOrderSlotSummaryResponse::createFrom).collect(Collectors.toList())
                : null;
        List<MedicationSlotResponse> emergencyMedicationSlots = patientMedicationSummary.getEmergencyMedicationSlots() != null
                ? patientMedicationSummary.getEmergencyMedicationSlots().stream().map(MedicationSlotResponse::createFrom).collect(Collectors.toList())
                : null;
        return PatientMedicationSummaryResponse.builder()
                .patientUuid(patientMedicationSummary.getPatientUuid())
                .prescribedOrderSlots(prescribedOrderSlots)
                .emergencyMedicationSlots(emergencyMedicationSlots)
                .build();
    }
}
