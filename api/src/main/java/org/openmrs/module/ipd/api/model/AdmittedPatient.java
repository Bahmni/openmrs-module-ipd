/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.model;

import org.openmrs.module.bedmanagement.entity.BedPatientAssignment;

public class AdmittedPatient {

    private BedPatientAssignment bedPatientAssignment;
    private Long newTreatments;
    private CareTeam careTeam;


    public AdmittedPatient(BedPatientAssignment bedPatientAssignment, Long newTreatments, CareTeam careTeam){
        this.bedPatientAssignment=bedPatientAssignment;
        this.newTreatments=newTreatments;
        this.careTeam=careTeam;
    }

    public BedPatientAssignment getBedPatientAssignment() {
        return bedPatientAssignment;
    }

    public Long getNewTreatments() {
        return newTreatments;
    }

    public CareTeam getCareTeam() { return careTeam; }
}
