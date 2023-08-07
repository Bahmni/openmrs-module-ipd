package org.openmrs.module.ipd.api.model;

public enum ServiceType {
    MEDICATION_REQUEST("MedicationRequest");

    private final String conceptName;

    ServiceType(String medicationRequest) {
        this.conceptName = medicationRequest;
    }

    public String conceptName() {
        return conceptName;
    }
}