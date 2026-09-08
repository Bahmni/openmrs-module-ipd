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
import org.openmrs.module.ipd.api.model.CareTeam;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CareTeamResponse {

    private String uuid;
    private String patientUuid;
    private List<CareTeamParticipantResponse> participants;

    public static CareTeamResponse createFrom(CareTeam careTeam){
        return CareTeamResponse.builder().
                uuid(careTeam.getUuid()).
                patientUuid(careTeam.getPatient().getUuid()).
                participants(careTeam.getParticipants().stream().
                        filter(careTeamParticipant -> !careTeamParticipant.getVoided()).
                        map(CareTeamParticipantResponse::createFrom).collect(Collectors.toList())).
                build();
    }

}
