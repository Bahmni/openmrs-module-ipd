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
import org.openmrs.module.ipd.api.model.IPDPatientDetails;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
public class IPDPatientDetailsResponse {
    private List<AdmittedPatientResponse> admittedPatients;
    private Integer totalPatients;

    public static IPDPatientDetailsResponse createFrom(IPDPatientDetails ipdPatientDetails) {
        return IPDPatientDetailsResponse.builder()
                .admittedPatients(ipdPatientDetails.getAdmittedPatients().stream().map(AdmittedPatientResponse::createFrom).collect(Collectors.toList()))
                .totalPatients(ipdPatientDetails.getPatientCount())
                .build();
    }
}
