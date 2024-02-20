package org.openmrs.module.ipd.api.model;

import org.openmrs.module.bedmanagement.entity.BedPatientAssignment;

public class AdmittedPatient {

    private BedPatientAssignment bedPatientAssignment;
    private Long newTreatments;

    public AdmittedPatient(BedPatientAssignment bedPatientAssignment, Long newTreatments){
        this.bedPatientAssignment=bedPatientAssignment;
        this.newTreatments=newTreatments;
    }

    public BedPatientAssignment getBedPatientAssignment() {
        return bedPatientAssignment;
    }

    public Long getNewTreatments() {
        return newTreatments;
    }

}
