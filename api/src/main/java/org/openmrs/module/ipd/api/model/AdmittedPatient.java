package org.openmrs.module.ipd.api.model;

import org.openmrs.module.bedmanagement.entity.BedPatientAssignment;

public class AdmittedPatient extends BedPatientAssignment {

    private Long newTreatments;

    public AdmittedPatient(BedPatientAssignment bedPatientAssignment, Long newTreatments){
        super.setId(bedPatientAssignment.getId());
        super.setBed(bedPatientAssignment.getBed());
        super.setPatient(bedPatientAssignment.getPatient());
        super.setEncounter(bedPatientAssignment.getEncounter());
        super.setStartDatetime(bedPatientAssignment.getStartDatetime());
        super.setEndDatetime(bedPatientAssignment.getEndDatetime());
        super.setUuid(bedPatientAssignment.getUuid());
        super.setVoided(bedPatientAssignment.getVoided());
        super.setVoidReason(bedPatientAssignment.getVoidReason());
        this.newTreatments=newTreatments;
    }

    public void setNewTreatments(Long newTreatments) {
        this.newTreatments = newTreatments;
    }

    public Long getNewTreatments() {
        return newTreatments;
    }

}
