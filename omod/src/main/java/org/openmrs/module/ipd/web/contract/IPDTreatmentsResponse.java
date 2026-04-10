/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.contract;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPDTreatmentsResponse {

    private List<IPDDrugOrderResponse> ipdDrugOrders;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<MedicationAdministrationResponse> emergencyMedications;

    public static IPDTreatmentsResponse createFrom(List<IPDDrugOrderResponse> ipdDrugOrders, List<MedicationAdministrationResponse> emergencyMedications) {
            return IPDTreatmentsResponse.builder()
                    .ipdDrugOrders(ipdDrugOrders)
                    .emergencyMedications(emergencyMedications)
                    .build();
    }
}
