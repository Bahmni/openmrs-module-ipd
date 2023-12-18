package org.openmrs.module.ipd.service.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.openmrs.module.fhir2.apiext.FhirMedicationAdministrationService;
import org.openmrs.module.fhir2.apiext.dao.FhirMedicationAdministrationDao;
import org.openmrs.module.fhir2.apiext.search.param.MedicationAdministrationSearchParams;
import org.openmrs.module.fhir2.apiext.translators.MedicationAdministrationTranslator;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.contract.MedicationAdministrationResponse;
import org.openmrs.module.ipd.factory.MedicationAdministrationFactory;
import org.openmrs.module.ipd.service.IPDMedicationAdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Transactional
@Service
public class IPDMedicationAdministrationServiceImpl implements IPDMedicationAdministrationService {


    private FhirMedicationAdministrationService fhirMedicationAdministrationService;
    private MedicationAdministrationFactory medicationAdministrationFactory;
    private SlotService slotService;
    private FhirMedicationAdministrationDao fhirMedicationAdministrationDao;

    @Autowired
    public IPDMedicationAdministrationServiceImpl(FhirMedicationAdministrationService fhirMedicationAdministrationService,
                                                  MedicationAdministrationFactory medicationAdministrationFactory,
                                                  SlotService slotService,
                                                  FhirMedicationAdministrationDao fhirMedicationAdministrationDao) {
        this.fhirMedicationAdministrationService = fhirMedicationAdministrationService;
        this.medicationAdministrationFactory = medicationAdministrationFactory;
        this.slotService = slotService;
        this.fhirMedicationAdministrationDao = fhirMedicationAdministrationDao;
    }


    @Override
    public MedicationAdministration saveMedicationAdministration(MedicationAdministrationRequest medicationAdministrationRequest) {
        Slot slot = slotService.getSlotByUUID(medicationAdministrationRequest.getSlotUuid());

        if(slot.getMedicationAdministration() != null){
            return fhirMedicationAdministrationService.get(slot.getMedicationAdministration().getUuid());
        } else {
            MedicationAdministration medicationAdministration = medicationAdministrationFactory.createMedicationAdministrationFrom(medicationAdministrationRequest);
            medicationAdministration = fhirMedicationAdministrationService.create(medicationAdministration);

            if (medicationAdministration.getStatus().equals(org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationStatus.COMPLETED)) {
                slot.setStatus(Slot.SlotStatus.COMPLETED);
            }
            slot.setMedicationAdministration((org.openmrs.module.fhir2.model.MedicationAdministration) fhirMedicationAdministrationDao.get(medicationAdministration.getId()));
            slotService.saveSlot(slot);
            System.out.println("after saving slot ********* " + slot.getMedicationAdministration().getUuid());
            return medicationAdministration;
        }

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
