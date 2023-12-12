package org.openmrs.module.ipd.service.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.openmrs.module.fhir2.apiext.FhirMedicationAdministrationService;
import org.openmrs.module.fhir2.apiext.search.param.MedicationAdministrationSearchParams;
import org.openmrs.module.ipd.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.contract.MedicationAdministrationResponse;
import org.openmrs.module.ipd.factory.MedicationAdministrationFactory;
import org.openmrs.module.ipd.service.IPDMedicationAdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Transactional
@Service
public class IPDMedicationAdministrationServiceImpl implements IPDMedicationAdministrationService {


    private FhirMedicationAdministrationService fhirMedicationAdministrationService;
    private MedicationAdministrationFactory medicationAdministrationFactory;

    @Autowired
    public IPDMedicationAdministrationServiceImpl(FhirMedicationAdministrationService fhirMedicationAdministrationService, MedicationAdministrationFactory medicationAdministrationFactory) {
        this.fhirMedicationAdministrationService = fhirMedicationAdministrationService;
        this.medicationAdministrationFactory = medicationAdministrationFactory;
    }


    @Override
    public List<MedicationAdministrationResponse> saveMedicationAdministration(List<MedicationAdministrationRequest> medicationAdministrationRequestList) {

        List<MedicationAdministrationResponse> medicationAdministrationsResponse = medicationAdministrationRequestList.stream().
                map(medicationAdministrationFactory::createMedicationAdministrationFrom).
                map(fhirMedicationAdministrationService::create).
                map(medicationAdministrationFactory::createFrom).collect(Collectors.toList());
        return medicationAdministrationsResponse;
    }

    @Override
    public List<MedicationAdministrationResponse> getMedicationAdministrationList(String patientUuid, LocalDate forDate, String providerUuid, String slotUuid) {
        ReferenceParam patient = new ReferenceParam();
        patient.setValue("Patient/"+patientUuid);
        ReferenceAndListParam subjectReference = new ReferenceAndListParam();
        subjectReference.addValue(new ReferenceOrListParam().add(patient));

        ReferenceParam slot = new ReferenceParam();
        patient.setValue("Slot/"+slotUuid);
        ReferenceAndListParam supportInfoReference = new ReferenceAndListParam();
        supportInfoReference.addValue(new ReferenceOrListParam().add(patient));


        MedicationAdministrationSearchParams medicationAdministrationSearchParams = MedicationAdministrationSearchParams.builder().patientReference(subjectReference).supportingInfoReference(supportInfoReference).build();
        IBundleProvider response = fhirMedicationAdministrationService.searchForMedicationAdministration(medicationAdministrationSearchParams);
        List<IBaseResource> responseData = response.getAllResources();
        List<MedicationAdministrationResponse> medicationAdministrationsResponse= responseData.stream().
                map(iBaseResource -> (MedicationAdministration)iBaseResource).
                map(medicationAdministrationFactory::createFrom).collect(Collectors.toList());
        return medicationAdministrationsResponse;
    }

}
