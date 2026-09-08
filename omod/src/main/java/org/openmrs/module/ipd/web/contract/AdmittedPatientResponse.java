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
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

@Getter
@Setter
@Builder
public class AdmittedPatientResponse {

    private Object patientDetails;
    private Object bedDetails;
    private Object visitDetails;
    private Long newTreatments;
    private Object careTeam;
    public static AdmittedPatientResponse createFrom(AdmittedPatient admittedPatient) {
        AdmittedPatientResponse admittedPatientResponse =  AdmittedPatientResponse.builder().
                patientDetails(ConversionUtil.convertToRepresentation(admittedPatient.getBedPatientAssignment().getPatient(), Representation.DEFAULT)).
                bedDetails(ConversionUtil.convertToRepresentation(admittedPatient.getBedPatientAssignment().getBed(),Representation.REF)).
                visitDetails(ConversionUtil.convertToRepresentation(admittedPatient.getBedPatientAssignment().getEncounter().getVisit(), Representation.REF)).
                newTreatments(admittedPatient.getNewTreatments()).build();

        if ( admittedPatient.getCareTeam() != null ) {
            admittedPatientResponse.setCareTeam(CareTeamResponse.createFrom(admittedPatient.getCareTeam()));
        }

        return admittedPatientResponse;
    }
}
