package org.openmrs.module.ipd.service;

import org.hl7.fhir.r4.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.contract.MedicationAdministrationResponse;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;

import java.time.LocalDate;
import java.util.List;

public interface IPDMedicationAdministrationService {

    MedicationAdministrationResponse saveMedicationAdministration(MedicationAdministrationRequest medicationAdministrationRequest);

    List<MedicationAdministrationResponse> getMedicationAdministrationList(String orderUuid, LocalDate forDate);

}
