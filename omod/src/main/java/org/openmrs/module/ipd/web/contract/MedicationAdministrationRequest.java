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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationAdministrationRequest {

    private String uuid;
    private String patientUuid;
    private String encounterUuid;
    private String orderUuid;
    private List<MedicationAdministrationPerformerRequest> providers;
    private List<MedicationAdministrationNoteRequest> notes;
    private String status;
    private String statusReason;
    private String drugUuid;
    private String dosingInstructions;
    private Double dose;
    private String doseUnits;
    private String route;
    private String site;
    private Long administeredDateTime;
    private String slotUuid;

    public Date getAdministeredDateTimeAsLocaltime() {
        return this.administeredDateTime != null ? new Date(TimeUnit.SECONDS.toMillis(this.administeredDateTime)): new Date();
    }
}
