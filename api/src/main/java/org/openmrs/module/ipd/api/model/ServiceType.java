package org.openmrs.module.ipd.api.model;

public enum ServiceType {
    MEDICATION_REQUEST("MedicationRequest"),
    EMERGENCY_MEDICATION_REQUEST("EmergencyMedicationRequest"),
    AS_NEEDED_MEDICATION_REQUEST("AsNeededMedicationRequest");

    private final String conceptName;

    ServiceType(String medicationRequest) {
        this.conceptName = medicationRequest;
    }

    public String conceptName() {
        return conceptName;
    }
}