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
import org.openmrs.module.ipd.api.model.CareTeamParticipant;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

@Getter
@Builder
public class CareTeamParticipantResponse {

    private String uuid;
    private Object provider;
    private Long startTime;
    private Long endTime;
    private Boolean voided;

    public static CareTeamParticipantResponse createFrom(CareTeamParticipant careTeamParticipant) {
        return CareTeamParticipantResponse.builder().
                uuid(careTeamParticipant.getUuid()).
                provider(ConversionUtil.convertToRepresentation(careTeamParticipant.getProvider(), Representation.REF)).
                startTime(careTeamParticipant.getStartTime().getTime()).
                endTime(careTeamParticipant.getEndTime().getTime()).
                voided(careTeamParticipant.getVoided()).
                build();
    }

}
