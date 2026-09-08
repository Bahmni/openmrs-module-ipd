/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.contract;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

@Getter
@Setter
@Builder
public class IPDWardPatientSummaryResponse {

    private Long totalPatients;
    private Long totalProviderPatients;

    public static IPDWardPatientSummaryResponse createFrom(WardPatientsSummary wardPatientsSummary){
        return IPDWardPatientSummaryResponse.builder().
                totalPatients(wardPatientsSummary.getTotalPatients()).
                totalProviderPatients(wardPatientsSummary.getTotalProviderPatients()).
                build();
    }
}
