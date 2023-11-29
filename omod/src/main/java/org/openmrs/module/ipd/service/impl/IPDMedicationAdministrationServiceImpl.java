package org.openmrs.module.ipd.service.impl;

import org.hl7.fhir.r4.model.MedicationAdministration;
import org.openmrs.module.fhir2.api.FhirMedicationAdministrationService;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.contract.MedicationAdministrationResponse;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.factory.MedicationAdministrationFactory;
import org.openmrs.module.ipd.service.IPDMedicationAdministrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Transactional
@Service
public class IPDMedicationAdministrationServiceImpl implements IPDMedicationAdministrationService {

    private FhirMedicationAdministrationService fhirMedicationAdministrationService;
    private MedicationAdministrationFactory medicationAdministrationFactory;

    public IPDMedicationAdministrationServiceImpl(FhirMedicationAdministrationService fhirMedicationAdministrationService,MedicationAdministrationFactory medicationAdministrationFactory){
        this.fhirMedicationAdministrationService=fhirMedicationAdministrationService;
        this.medicationAdministrationFactory=medicationAdministrationFactory;
    }


    @Override
    public MedicationAdministrationResponse saveMedicationAdministration(MedicationAdministrationRequest medicationAdministrationRequest) {
        MedicationAdministration medicationAdministration= medicationAdministrationFactory.createMedicationAdministrationFrom(medicationAdministrationRequest);
        MedicationAdministration savedMedicationAdministration=fhirMedicationAdministrationService.create(medicationAdministration);
        return medicationAdministrationFactory.createFrom(savedMedicationAdministration);
    }

    @Override
    public List<MedicationAdministrationResponse> getMedicationAdministrationList(String orderUuid, LocalDate forDate) {
        return null;
    }
}
