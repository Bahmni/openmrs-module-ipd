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
