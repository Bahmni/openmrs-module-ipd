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
import org.openmrs.module.ipd.api.model.IPDWardPatientDetails;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class IPDWardPatientDetailsResponse {

    private List<AdmittedPatientResponse> admittedPatients;
    private Long totalPatients;


    public static IPDWardPatientDetailsResponse createFrom(IPDWardPatientDetails ipdWardPatientDetails) {
        return IPDWardPatientDetailsResponse.builder().
                admittedPatients(ipdWardPatientDetails.getActivePatients().stream().map(AdmittedPatientResponse::createFrom).collect(Collectors.toList())).
                totalPatients(ipdWardPatientDetails.getIpdWardWardPatientsSummary().getTotalPatients()).
                build();
    }
}
