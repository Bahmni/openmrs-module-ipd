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
import org.openmrs.module.bedmanagement.AdmissionLocation;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IPDWardResponse {
    private String uuid;
    private String name;

    public static IPDWardResponse createFrom (AdmissionLocation admissionLocation){
        return IPDWardResponse.builder().
                uuid(admissionLocation.getWard().getUuid()).
                name(admissionLocation.getWard().getName()).
                build();
    }

}
