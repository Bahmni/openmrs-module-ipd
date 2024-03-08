package org.openmrs.module.ipd.contract;

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
    private Long newTreatments;
    private Object careTeam;
    public static AdmittedPatientResponse createFrom(AdmittedPatient admittedPatient) {
        AdmittedPatientResponse admittedPatientResponse =  AdmittedPatientResponse.builder().
                patientDetails(ConversionUtil.convertToRepresentation(admittedPatient.getBedPatientAssignment().getPatient(), Representation.DEFAULT)).
                bedDetails(ConversionUtil.convertToRepresentation(admittedPatient.getBedPatientAssignment().getBed(),Representation.REF)).
                newTreatments(admittedPatient.getNewTreatments()).build();

        if ( admittedPatient.getCareTeam() != null ) {
            admittedPatientResponse.setCareTeam(CareTeamResponse.createFrom(admittedPatient.getCareTeam()));
        }

        return admittedPatientResponse;
    }
}
