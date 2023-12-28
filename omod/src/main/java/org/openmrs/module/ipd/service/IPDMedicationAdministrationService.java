package org.openmrs.module.ipd.service;

import org.hl7.fhir.r4.model.MedicationAdministration;
import org.openmrs.module.ipd.contract.MedicationAdministrationRequest;

public interface IPDMedicationAdministrationService {

    MedicationAdministration saveScheduledMedicationAdministration(MedicationAdministrationRequest medicationAdministrationRequest);

    MedicationAdministration saveAdhocMedicationAdministration(MedicationAdministrationRequest medicationAdministrationRequest);

}
