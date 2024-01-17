package org.openmrs.module.ipd.service.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.apiext.FhirMedicationAdministrationService;
import org.openmrs.module.fhir2.apiext.dao.FhirMedicationAdministrationDao;
import org.openmrs.module.fhir2.apiext.translators.MedicationAdministrationTranslator;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.openmrs.module.ipd.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.factory.MedicationAdministrationFactory;
import org.openmrs.module.ipd.factory.SlotFactory;
import org.openmrs.module.ipd.service.IPDMedicationAdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Transactional
@Service
public class IPDMedicationAdministrationServiceImpl implements IPDMedicationAdministrationService {


    private FhirMedicationAdministrationService fhirMedicationAdministrationService;
    private MedicationAdministrationTranslator medicationAdministrationTranslator;
    private MedicationAdministrationFactory medicationAdministrationFactory;
    private SlotFactory slotFactory;
    private SlotService slotService;
    private ScheduleService scheduleService;
    private FhirMedicationAdministrationDao fhirMedicationAdministrationDao;

    @Autowired
    public IPDMedicationAdministrationServiceImpl(FhirMedicationAdministrationService fhirMedicationAdministrationService,
                                                  MedicationAdministrationTranslator medicationAdministrationTranslator,
                                                  MedicationAdministrationFactory medicationAdministrationFactory,
                                                  SlotFactory slotFactory, SlotService slotService, ScheduleService scheduleService,
                                                  FhirMedicationAdministrationDao fhirMedicationAdministrationDao) {
        this.fhirMedicationAdministrationService = fhirMedicationAdministrationService;
        this.medicationAdministrationTranslator = medicationAdministrationTranslator;
        this.medicationAdministrationFactory = medicationAdministrationFactory;
        this.slotFactory = slotFactory;
        this.slotService = slotService;
        this.scheduleService = scheduleService;
        this.fhirMedicationAdministrationDao = fhirMedicationAdministrationDao;
    }

    private org.hl7.fhir.r4.model.MedicationAdministration createMedicationAdministration(MedicationAdministrationRequest medicationAdministrationRequest) {
        MedicationAdministration medicationAdministration = medicationAdministrationFactory.mapRequestToMedicationAdministration(medicationAdministrationRequest, new MedicationAdministration());
        return fhirMedicationAdministrationService.create(medicationAdministrationTranslator.toFhirResource(medicationAdministration));
    }

    @Override
    public org.hl7.fhir.r4.model.MedicationAdministration saveScheduledMedicationAdministration(MedicationAdministrationRequest medicationAdministrationRequest) {
        Slot slot = slotService.getSlotByUUID(medicationAdministrationRequest.getSlotUuid());
        if (slot == null) {
            throw new RuntimeException("Slot not found");
        } else {
            if (slot.getMedicationAdministration() != null) {
                return fhirMedicationAdministrationService.get(slot.getMedicationAdministration().getUuid());
            } else if (!StringUtils.isBlank(medicationAdministrationRequest.getUuid())) {
                return fhirMedicationAdministrationService.get(medicationAdministrationRequest.getUuid());
            } else {
                org.hl7.fhir.r4.model.MedicationAdministration medicationAdministration = createMedicationAdministration(medicationAdministrationRequest);
                if (medicationAdministration.getStatus().equals(org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationStatus.COMPLETED)) {
                    slot.setStatus(Slot.SlotStatus.COMPLETED);
                }
                slot.setMedicationAdministration((MedicationAdministration) fhirMedicationAdministrationDao.get(medicationAdministration.getId()));
                slotService.saveSlot(slot);
                return medicationAdministration;
            }
        }
    }

    @Override
    public org.hl7.fhir.r4.model.MedicationAdministration saveAdhocMedicationAdministration(MedicationAdministrationRequest medicationAdministrationRequest) {
        Patient patient = Context.getPatientService().getPatientByUuid(medicationAdministrationRequest.getPatientUuid());
        Visit visit = Context.getVisitService().getActiveVisitsByPatient(patient).get(0);
        Schedule schedule = scheduleService.getScheduleByVisit(visit);
        if (schedule == null) {
            throw new RuntimeException("Active Schedule not found");
        } else {
            org.hl7.fhir.r4.model.MedicationAdministration medicationAdministration = createMedicationAdministration(medicationAdministrationRequest);
            MedicationAdministration openmrsMedicationAdministration = (MedicationAdministration) fhirMedicationAdministrationDao.get(medicationAdministration.getId());
            List<LocalDateTime> slotsStartTime = new ArrayList<>();
            slotsStartTime.add(DateTimeUtil.convertEpocUTCToLocalTimeZone(medicationAdministrationRequest.getAdministeredDateTime()));
            ServiceType serviceType = openmrsMedicationAdministration.getDrugOrder() == null ? ServiceType.EMERGENCY_MEDICATION_REQUEST : ServiceType.AS_NEEDED_MEDICATION_REQUEST;
            slotFactory.createSlotsForMedicationFrom(schedule, slotsStartTime, openmrsMedicationAdministration.getDrugOrder(),
                            openmrsMedicationAdministration, Slot.SlotStatus.COMPLETED, serviceType, "")
                    .forEach(slotService::saveSlot);
            return medicationAdministration;
        }
    }

}
